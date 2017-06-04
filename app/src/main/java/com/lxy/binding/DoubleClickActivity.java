package com.lxy.binding;

import android.Manifest;
import android.databinding.DataBindingUtil;
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
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DoubleClickActivity extends AppCompatActivity {

    ActivityDoubleClickBinding mBinding;

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
                //textChange 1秒之后执行网络操作
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

        //倒计时



        //动态权限
        RxPermissions permissions = new RxPermissions(this);
        RxView.clicks(mBinding.btPermissionCheck)
                .throttleFirst(1,TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(permissions.ensure(Manifest.permission.CAMERA))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            System.out.println("binding=======允许");
                        }else {
                            System.out.println("binding=======拒绝");
                        }
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


}
