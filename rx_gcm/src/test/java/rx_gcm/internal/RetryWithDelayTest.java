/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx_gcm.internal;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RetryWithDelayTest {
    private int errorTimes;
    private int counterErrorNotified;

    @Test public void Check_Retry_With_Delay() {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                subscriber.onError(new RuntimeException(""));
            }
        }).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                errorTimes++;
            }
        }).retryWhen(new RetryWithDelay(3, 50)).subscribe(new Subscriber<String>() {
            @Override public void onCompleted() {}

            @Override public void onError(Throwable e) {
                counterErrorNotified++;
            }

            @Override public void onNext(String s) {}
        });


        assertThat(errorTimes, is(1));
        wait55Millis();
        assertThat(errorTimes, is(2));
        wait55Millis();
        assertThat(errorTimes, is(3));
        wait55Millis();
        assertThat(counterErrorNotified, is(1));
    }

    private void wait55Millis() {
        try {
            Thread.sleep(55);
        } catch (InterruptedException e) {}
    }

}
