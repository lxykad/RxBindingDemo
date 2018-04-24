package com.lxy.binding;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lxy.binding.databinding.ActivityDoubleClickBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author a
 */
public class DoubleClickActivity extends AppCompatActivity {

    ActivityDoubleClickBinding mBinding;
    private Disposable mDisposable;
    private Disposable mIntervalDisposable;
    private int count;//轮询次数
    private int mRetryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_double_click);

        //防止多次点击--3秒内执行一次点击
        RxView.clicks(mBinding.tvClick)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        System.out.println("binding=======点击了按钮");
                    }
                });

        //长按事件
        RxView.longClicks(mBinding.tvClick)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        System.out.println("binding=======长按了按钮");
                    }
                });

        //checkbox 选中就修改textview
        RxCompoundButton.checkedChanges(mBinding.checkbox)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        mBinding.tvCb.setText(aBoolean ? "按钮选中了" : "按钮未选中");
                    }
                });

        //优化搜索功能
        RxTextView.textChanges(mBinding.etSearch)
                // 跳过一开始et内容为空时的搜索
                .skip(1)
                //debounce 在一定的时间内没有操作就会发送事件
                .debounce(1000, TimeUnit.MILLISECONDS)
                //下面这两个都是数据转换
                //flatMap：当同时多个网络请求访问的时候，前面的网络数据会覆盖后面的网络数据
                //switchMap：当同时多个网络请求访问的时候，会以最后一个发送请求为准，前面网路数据会被最后一个覆盖
                .switchMap(new Function<CharSequence, ObservableSource<List<String>>>() {
                    @Override
                    public ObservableSource<List<String>> apply(CharSequence charSequence) throws Exception {
                        String searchKey = charSequence.toString();
                        System.out.println("binding=======搜索内容:" + searchKey);
                        //这里执行网络操作，获取数据
                        List<String> list = new ArrayList<String>();
                        list.add("小刘哥");
                        list.add("可爱多");

                        return Observable.just(list);
                    }
                })
                // .onErrorResumeNext()
                //网络操作，获取我们需要的数据
                .subscribeOn(Schedulers.io())
                //界面更新在主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        System.out.println("binding=======搜索到" + strings.size() + "条数据");
                    }
                });

        //动态权限
        RxPermissions permissions = new RxPermissions(this);
        RxView.clicks(mBinding.btPermissionCheck)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(permissions.ensure(Manifest.permission.CAMERA))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            System.out.println("binding=======允许");
                        } else {
                            System.out.println("binding=======拒绝");
                        }
                    }
                });

        /**
         * combineLatest操作符 合并n个节点
         * 如 账号 密码 都输入合法才点亮登录按钮
         *
         */
        Observable<CharSequence> name = RxTextView.textChanges(mBinding.etName).skip(1);
        Observable<CharSequence> age = RxTextView.textChanges(mBinding.etAge).skip(1);

        Observable.combineLatest(name, age, new BiFunction<CharSequence, CharSequence, Boolean>() {
            @Override
            public Boolean apply(CharSequence charSequence, CharSequence charSequence2) throws Exception {

                boolean isNameEmpty = TextUtils.isEmpty(mBinding.etName.getText());
                boolean isAgeEmpty = TextUtils.isEmpty(mBinding.etAge.getText());

                return !isNameEmpty && !isAgeEmpty;
            }
        })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        System.out.println("bt======" + aBoolean);
                        mBinding.btSubmit.setEnabled(aBoolean);
                    }
                });


        // RxBus


        //merge合并数据源


        //使用concat和first做缓存


        //使用interval做周期性操作  每隔3秒 输出一次日志
        Observable.interval(2, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {


                    }

                    @Override
                    public void onNext(Long value) {
                        System.out.println("binding=======输出日志:" + value);
                        if (value == 5l) {
                            System.out.println("binding=======dispose");
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        //使用schedulePeriodically做轮询请求
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {

                Schedulers.newThread().createWorker()
                        .schedulePeriodically(new Runnable() {
                            @Override
                            public void run() {
                                e.onNext("net work-----");
                            }
                        }, 0, 3, TimeUnit.SECONDS);

            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                System.out.println("binding=======net work");
            }
        });

        /**
         * 无条件 无限轮询
         *
         * params1 : 延迟时间
         * params2 ：间隔时间
         * 每隔1秒产生一个数字，0开始 递增
         *
         *
         * 每次发送数字前发送1次网络请求（doOnNext（）在执行Next事件前调用）
         *
         */
        Observable.interval(2, 1, TimeUnit.SECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        // 每隔1秒执行一次网络请求

                        System.out.println("interval====doOnNext===" + aLong);
                    }
                }).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                mIntervalDisposable = d;
            }

            @Override
            public void onNext(Long value) {
                System.out.println("interval====onNext===" + value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        /**
         * 条件轮询 repeatWhen
         *
         */
        Observable.just(100)
                .repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {

                        // 将原始 Observable 停止发送事件的标识（Complete（） /  Error（））转换成1个 Object 类型数据传递给1个新被观察者（Observable）
                        // 以此决定是否重新订阅 & 发送原来的 Observable，即轮询
                        return objectObservable.flatMap(new Function<Object, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Object o) throws Exception {
                                if (count > 4) {
                                    return Observable.error(new Throwable("轮询完成"));
                                }

                                return Observable.just(1)
                                        .delay(2, TimeUnit.SECONDS);
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer value) {
                        System.out.println("repeat=====" + value);
                        count++;
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("repeat====err=" + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("repeat=====complete");
                    }
                });

        /**
         * 网络重试
         * 这里just操作符 改为retrofit 网络请求返回的即可。
         */
        Observable.just("retry")
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {

                        // 参数Observable<Throwable>中的泛型 = 上游操作符抛出的异常，可通过该条件来判断异常的类型
                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Throwable throwable) throws Exception {

                                // 判断异常信息  根据异常信息判断是否需要重试
                                if (throwable instanceof IOException) {
                                    System.out.println("retry======y==");
                                    // 重试
                                    // 判断重试次数 这里设置最多重试5次
                                    if (mRetryCount < 5) {
                                        mRetryCount++;
                                        /**
                                         * 1、通过返回的Observable发送的事件 = Next事件，从而使得retryWhen（）重订阅，最终实现重试功能
                                         * 2、延迟1段时间再重试  采用delay操作符 = 延迟一段时间发送，以实现重试间隔设置
                                         * 3、在delay操作符的等待时间内设置 = 每重试1次，增多延迟重试时间1s
                                         */
                                        int time = 1000 + mRetryCount * 1000;
                                        return Observable.just(1).delay(time, TimeUnit.MILLISECONDS);
                                    } else {
                                        System.out.println("retry======5==");
                                        return Observable.error(new Throwable("已重试5次 放弃治疗"));
                                    }

                                } else {
                                    // 不重试
                                    System.out.println("retry======n==");
                                    return Observable.error(new Throwable("发生了非网络异常（非I/O异常）"));
                                }
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                        System.out.println("retry======suc==" + value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("retry======err==" + e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        /**
         * flatmap 变换操作符解决 嵌套网络请求
         * 以下为 伪代码
         */
        Observable<String> requestLogin = Observable.just("requestLogin");
        final Observable<String> request2 = Observable.just("request2");

        requestLogin.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("flat=======loginsuccess");
                    }
                })
                .observeOn(Schedulers.io())
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        // 将网络请求1转换成网络请求2，即发送网络请求2
                        return request2;
                    }
                })
                // （新被观察者，同时也是新观察者）切换到IO线程去发起登录请求
                //  特别注意：因为flatMap是对初始被观察者作变换，所以对于旧被观察者，它是新观察者，所以通过observeOn切换线程
                // 但对于初始观察者，它则是新的被观察者
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println("flat=======第二次请求成功");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("flat=======loginerr");
                    }
                });

    }

    public void clickCount(View v) {
/*
        //一段时间（400毫秒）内的点击次数
        RxView.clicks(mBinding.tvClickCount)
                .share()
                .buffer(400, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<List<Object>>() {
                    @Override
                    public void accept(List<Object> objects) throws Exception {
                        System.out.println("binding=======400毫秒点击了" + objects.size() + "次");
                    }
                });*/
    }

    public void clickTimer(View view) {

        // 2 秒后发送数据
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long value) {
                        System.out.println("binding=======value:" + value);//0
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        //倒计时操作
        final int count = 10;
        Observable.interval(0, 1, TimeUnit.SECONDS)//设置0延迟，每隔一秒发送一条数据
                .take(count + 1)//设置循环次数
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {

                        return count - aLong;
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        //在发送数据的时候设置为不能点击
                        mBinding.btCutdown.setEnabled(false);

                        //背景色
                        mBinding.btCutdown.setBackgroundColor(Color.parseColor("#39c6c1"));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long value) {
                        mBinding.btCutdown.setText("" + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        mBinding.btCutdown.setText("重新获取");
                        mBinding.btCutdown.setEnabled(true);
                        mBinding.btCutdown.setBackgroundColor(Color.parseColor("#d1d1d1"));
                    }
                });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mIntervalDisposable != null) {
            mIntervalDisposable.dispose();
        }

    }
}
