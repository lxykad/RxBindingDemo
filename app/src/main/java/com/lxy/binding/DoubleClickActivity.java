package com.lxy.binding;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.lxy.binding.databinding.ActivityDoubleClickBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DoubleClickActivity extends AppCompatActivity {

    ActivityDoubleClickBinding mBinding;
    private Disposable mDisposable;

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


        //combineLatest 合并n个节点
        // 如 账号 密码 都输入合法才点亮登录按钮


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

}
