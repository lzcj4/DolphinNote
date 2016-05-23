package com.tannuo.note.whiteboard;

import java.util.Arrays;

public class Smooth {
    public int[] x;
    private int _x0, _y0;
    public int[] y;
    public int[] ptn;
    private double DirectX, DirectY;
    public int Pts;
    public int PtsTrue;
    public double minDist, maxDist; //^2? the min of dist between x2 and x1
    public double Dist;
    public int[][] interpolateArray;
    public int interpolateNum;
    private final String TAG = "Smooth";

    public Smooth() {
        x = new int[3];
        y = new int[3];
        ptn = new int[3];
        _x0 = x[0] = x[1] = x[1] = 0;
        _y0 = y[0] = y[1] = y[2] = 0;
        ptn[0] = ptn[1] = ptn[2] = -1;
        Pts = 0;
        PtsTrue = 0;
        DirectX = DirectY = 0;
        minDist = 90000;
        maxDist = 1600000;
        Dist = 0;
        interpolateNum = 0;
        interpolateArray = new int[2][4];
    }

    public int[][] SmoothLine(int[][] Points) {
        int i, j, pindex = 0;
        int[][] pointSet;

        pointSet = new int[4 * Points.length][];
        for (i = 0; i < Points.length; i++) {
            int state = callSmoothDistance(Points[i][0], Points[i][1], Points[i][2]);
            //Log.v(TAG,"The state is " + state);
            if (state == 0 || state == 4) {
                pointSet[pindex] = new int[3];
                pointSet[pindex][0] = x[2];
                pointSet[pindex][1] = y[2];
                pointSet[pindex][2] = Points[i][2];
                pindex++;
            } else if (state == 1 || state == 3) {
                for (j = 0; j < state; j++) {
                    pointSet[pindex] = new int[3];
                    pointSet[pindex][0] = interpolateArray[0][j];
                    pointSet[pindex][1] = interpolateArray[1][j];
                    pointSet[pindex][2] = Points[i][2];
                    pindex++;
                }
                pointSet[pindex] = new int[3];
                pointSet[pindex][0] = Points[i][0];
                pointSet[pindex][1] = Points[i][1];
                pointSet[pindex][2] = Points[i][2];
                pindex++;
            } else {
                pointSet[pindex] = new int[3];
                pointSet[pindex][0] = Points[i][0];
                pointSet[pindex][1] = Points[i][1];
                pointSet[pindex][2] = Points[i][2];
                pindex++;
            }

        }
        int[][] newData;
        newData = Arrays.copyOfRange(pointSet, 0, pindex);
        //Log.v(TAG,"The length of newData is " + newData.length);
        return newData;
    }

    public int callSmoothDistance(int newX, int newY, int ptNum) {
        // 5: no update; 4: shortdistance;
        // 0: interpolate 0; 1: interpolate 1 3: interpolate 3
        if (Pts == 3) { // update
            _x0 = x[0];
            x[0] = x[1];
            x[1] = x[2];
            x[2] = newX;
            _y0 = y[0];
            y[0] = y[1];
            y[1] = y[2];
            y[2] = newY;
            ptn[0] = ptn[1];
            ptn[1] = ptn[2];
            ptn[2] = ptNum;
            PtsTrue = 4;

            if (ptn[2] == ptn[1]) {
                Dist = (x[2] - x[1]) * (x[2] - x[1]) + (y[2] - y[1]) * (y[2] - y[1]);
                if (Dist < maxDist) {
                    SmoothShortDistance();
                    return 4;
                } else {
                    SmoothLongDistance();
                    return interpolateNum;
                }
            } else {
                SmoothClear();
                x[Pts] = newX;
                y[Pts] = newY;
                ptn[Pts] = ptNum;
                Pts++;
                return 5;
            }

        } else if (Pts == 2) {
            x[Pts] = newX;
            y[Pts] = newY;
            ptn[Pts] = ptNum;
            Pts++;

            if (ptn[Pts - 1] == ptn[Pts - 2]) {
                Dist = (x[2] - x[1]) * (x[2] - x[1]) + (y[2] - y[1]) * (y[2] - y[1]);
                if (Dist < maxDist) {
                    double value = Math.sqrt((x[1] - x[0]) * (x[1] - x[0]) + (y[1] - y[0]) * (y[1] - y[0]));
                    DirectX = (x[1] - x[0]) / value;
                    DirectY = (y[1] - y[0]) / value;
                    SmoothShortDistance();
                    return 4;
                } else {
                    SmoothLongDistance();
                    return interpolateNum;
                }
            } else {
                SmoothClear();
                x[Pts] = newX;
                y[Pts] = newY;
                ptn[Pts] = ptNum;
                Pts++;
                return 5;
            }
        } else { // add
            if (Pts == 1 && ptn[Pts - 1] != ptNum) {
                SmoothClear();
            }
            x[Pts] = newX;
            y[Pts] = newY;
            ptn[Pts] = ptNum;
            Pts++;
            return 5;
        }
    }

    private void SmoothShortDistance() {
        double Dist; //^2? the distance between x2 and x1
        double detx, dety; // the vetor from x1 to x2
        double prox, proy;
        double alpha;
        double tmp;
        Dist = (x[2] - x[1]) * (x[2] - x[1]) + (y[2] - y[1]) * (y[2] - y[1]);
        if (Dist < minDist) {
            x[2] = x[1];
            y[2] = y[1];
        } else if (DirectX > 1) { // impossible
            DirectX = x[2] - x[1];
            DirectY = y[2] - y[1];
            tmp = Math.sqrt(DirectX * DirectX + DirectY * DirectY);

            DirectX = DirectX / tmp;
            DirectY = DirectY / tmp;
        } else {
            detx = x[2] - x[1];
            dety = y[2] - y[1];

            tmp = detx * DirectX + dety * DirectY;
            prox = tmp * DirectX - detx;
            proy = tmp * DirectY - dety;

            alpha = Math.sqrt(minDist / Dist);
            tmp = prox * prox + proy * proy;
            if (tmp == 0.0)
                tmp = 0.8;
            else {
                tmp = alpha * minDist / tmp;
                if (tmp > 1.0)
                    tmp = 0.8;
                else
                    tmp = Math.sqrt(tmp);
            }

            x[2] = x[2] + (int) (prox * tmp);
            y[2] = y[2] + (int) (proy * tmp);

            DirectX = x[2] - x[1];
            DirectY = y[2] - y[1];

            tmp = Math.sqrt(DirectX * DirectX + DirectY * DirectY);

            if (tmp != 0.0) {
                DirectX /= tmp;
                DirectY /= tmp;
            }
        }
    }

    private void SmoothLongDistance() {
        int a0, a1, a2, b0, b1, b2, t;
        int c0, c1, c2, xx, yy;
        int tmpPoints;
        int i;
        if (Pts > 1) {
            xx = (x[2] - x[1]) * (x[2] - x[1]);
            yy = (y[2] - y[1]) * (y[2] - y[1]);
            tmpPoints = (Math.round(xx + yy)) >> 20;  // /64*64 = 4096, 32768
            //Log.v(TAG,"interpolateNum is" + interpolateNum);
            if (tmpPoints < 1) {
                //Points = 1;
                interpolateNum = 0;
            } else if (tmpPoints < 4) {
                //Points = 2;
                interpolateNum = 1;
            } else {//if(Points < 64)
                //Points = 4;
                interpolateNum = 3;
            }

//            if(Pts == 3 && PtsTrue == 4) {
//                c0 = (int)(_x0 + x[0] + x[1] +x[2]);
//                c1 = (int)(_x0 + 0*x[0] + x[1] +2*x[2]);
//                c2 = (int)(_x0 + 0*x[0] + x[1] +4*x[2]);
//                a0 = (int)(0.55*c0 + 0.15*c1 - 0.25*c2);
//                a1 = (int)(0.15*c0 + 0.45*c1 - 0.25*c2);
//                a2 = (int)(-0.25*c0 - 0.25*c1 + 0.25*c2);
//                xx = a0 + a1 + a2;
//                c0 = (int)(_y0 + y[0] + y[1] +y[2]);
//                c1 = (int)(_y0 + 0*y[0] + y[1] +2*y[2]);
//                c2 = (int)(_y0 + 0*y[0] + y[1] +4*y[2]);
//                b0 = (int)(0.55*c0 + 0.15*c1 - 0.25*c2);
//                b1 = (int)(0.15*c0 + 0.45*c1 - 0.25*c2);
//                b2 = (int)(-0.25*c0 - 0.25*c1 + 0.25*c2);
//                yy = b0 + b1 + b2;
//                x[1] = xx;
//                y[1] = yy;
//
//                for(i=1; i<=interpolateNum; i+=1) {
//                    t=i/(int)(interpolateNum+1);
//                    xx = a0 + a1*t + a2*t*t;
//                    yy = b0 + b1*t + b2*t*t;
//                    interpolateArray[0][i-1] = xx;
//                    interpolateArray[1][i-1] = yy;
//                    Log.v(TAG,"here");
//                    Log.v(TAG,"interpolateArray" + interpolateArray[0][i-1] + "to" +interpolateArray[1][i-1]);
//                }
//            }
            if (Pts == 3) {
                a0 = x[1];
                a1 = (x[2] - x[0]) / 2;
                a2 = (x[0] + x[2]) / 2 - x[1];
                b0 = y[1];
                b1 = (y[2] - y[0]) / 2;
                b2 = (y[0] + y[2]) / 2 - y[1];
//                Log.v("byt: interpolateNum", interpolateNum + "");
                for (i = 1; i <= interpolateNum; i++) {
                    xx = a0 + a1 * i / (interpolateNum + 1) + a2 * i * i / (interpolateNum + 1) / (interpolateNum + 1);
                    yy = b0 + b1 * i / (interpolateNum + 1) + b2 * i * i / (interpolateNum + 1) / (interpolateNum + 1);
                    interpolateArray[0][i - 1] = xx;
                    interpolateArray[1][i - 1] = yy;
                    //Log.v(TAG,"interpolateArray" + interpolateArray[0][i-1] + "to" +interpolateArray[1][i-1]);
                }
            }
        }
    }

    private void SmoothClear() {
        x[0] = x[1] = x[2] = 0;
        y[0] = y[1] = y[2] = 0;
        ptn[0] = ptn[1] = ptn[2] = -1;
        Pts = 0;
        DirectX = DirectY = 0;
        interpolateNum = 0;
        PtsTrue = 0;
    }


}
