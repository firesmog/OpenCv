package com.readboy.net;


public class RxNet {

    /**
     * 一般请求，返回数据带有body
     */

    /*public static <T> Disposable request(Observable<BaseResponse<T>> observable, final RxNetCallBack<T> callBack) {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, BaseResponse<T>>() {
                    @Override
                    public BaseResponse<T> apply(Throwable throwable) {
                        callBack.onFailure(ExceptionHandle.handleException(throwable));
                        return null;
                    }
                })
                .subscribe(new Consumer<BaseResponse<T>>() {
                    @Override
                    public void accept(BaseResponse<T> tBaseResponse) {
                        if (tBaseResponse.getResponse_code().equals("10000")) {
                            callBack.onSuccess(tBaseResponse.getResponse_data());
                        } else {
                            callBack.onFailure(tBaseResponse.getResponse_msg() + tBaseResponse.getResponse_code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                    }
                });
    }

    *//**
     * 返回数据没有body
     *//*

    public static Disposable requestWithoutBody(Observable<BaseResponse> observable,
                                                final RxNetCallBack<String> callBack) {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, BaseResponse>() {
                    @Override
                    public BaseResponse apply(Throwable throwable) {
                        callBack.onFailure(ExceptionHandle.handleException(throwable));
                        return null;
                    }
                })
                .subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse baseResponse) {
                        if (baseResponse.getResponse_code().equals("10000")) {
                            callBack.onSuccess(baseResponse.getResponse_msg());
                        } else {
                            callBack.onFailure(baseResponse.getResponse_msg() + baseResponse.getResponse_code());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });

    }


    *//**
     * 返回数据没有body
     *//*

    public static Disposable requestMessageConfirmCode(Observable<MessageResponse> observable,
                                                final RxNetCallBack<String> callBack) {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, MessageResponse>() {
                    @Override
                    public MessageResponse apply(Throwable throwable) {
                        callBack.onFailure(ExceptionHandle.handleException(throwable));
                        return null;
                    }
                })
                .subscribe(new Consumer<MessageResponse>() {
                    @Override
                    public void accept(MessageResponse messageResponse) throws Exception {
                        if (messageResponse.getResponse_code().equals("10000")) {
                            callBack.onSuccess(messageResponse.getResponse_msg());
                        } else {
                            callBack.onFailure(messageResponse.getResponse_msg() + messageResponse.getResponse_code());
                        }
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });

    }

    *//**
     * 请求返回错误请求码
     * -1表示请求失败
     *//*

    public static <T> Disposable requestForCode(Observable<BaseResponse<T>> observable, final RxNetCallBackForCode<T> callBack) {
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, BaseResponse<T>>() {
                    @Override
                    public BaseResponse<T> apply(Throwable throwable) {
                        // -1表示请求失败
                        callBack.onFailure("-1", ExceptionHandle.handleException(throwable));
                        return null;
                    }
                })
                .subscribe(new Consumer<BaseResponse<T>>() {
                    @Override
                    public void accept(BaseResponse<T> tBaseResponse) {
                        if (tBaseResponse.getResponse_code().equals("10000")) {
                            callBack.onSuccess(tBaseResponse.getResponse_data());
                        } else {
                            callBack.onFailure(tBaseResponse.getResponse_code(), tBaseResponse.getResponse_msg());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });
    }*/

    /**
     * 下载和上传
     */

}
