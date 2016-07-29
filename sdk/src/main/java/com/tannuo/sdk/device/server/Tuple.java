package com.tannuo.sdk.device.server;

/**
 * Created by Nick_PC on 2016/7/27.
 */
public class Tuple {

    public static class Tuple2<T1, T2> {
        private T1 item1;
        private T2 item2;

        public Tuple2(T1 t1, T2 t2) {
            item1 = t1;
            item2 = t2;
        }

        public T1 getItem1() {
            return item1;
        }

        public T2 getItem2() {
            return item2;
        }
    }


    public static class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
        private T3 item3;

        public Tuple3(T1 t1, T2 t2, T3 t3) {
            super(t1, t2);
            item3 = t3;

        }
    }
}
