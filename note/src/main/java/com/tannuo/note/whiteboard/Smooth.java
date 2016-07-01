package com.tannuo.note.whiteboard;

import java.util.Arrays;

public class Smooth {
    private final String TAG = this.getClass().getSimpleName();
    public int[] xAxes;
    public int[] yAxes;
    public int[] ptnIds;

    private int _x0, _y0;
    private double DirectX, DirectY;
    public int Pts;
    public int PtsTrue;
    public double minDist, maxDist; //^2? the min of dist between x2 and x1
    public double currentDist;
    public int[][] interpolateArray;
    public int interpolateNum;

    public Smooth() {
        xAxes = new int[3];
        yAxes = new int[3];
        ptnIds = new int[3];

        _x0 = xAxes[0] = xAxes[1] = xAxes[1] = 0;
        _y0 = yAxes[0] = yAxes[1] = yAxes[2] = 0;
        ptnIds[0] = ptnIds[1] = ptnIds[2] = -1;
        Pts = 0;
        PtsTrue = 0;
        DirectX = DirectY = 0;
        minDist = 90000;
        maxDist = 1600000;
        currentDist = 0;
        interpolateNum = 0;
        interpolateArray = new int[2][4];
    }

    public int[][] smoothLine(int[][] linePoints) {
        int index = 0, len = linePoints.length;
        int[][] pointSet = new int[4 * len][];

        for (int i = 0; i < len; i++) {
            int state = callSmoothDistance(linePoints[i][0], linePoints[i][1], linePoints[i][2]);
            //Log.v(TAG,"The state is " + state);
            if (state == 0 || state == 4) {
                pointSet[index] = new int[3];
                pointSet[index][0] = xAxes[2];
                pointSet[index][1] = yAxes[2];
                pointSet[index][2] = linePoints[i][2];
                index++;
                continue;
            } else if (state == 1 || state == 3) {
                for (int j = 0; j < state; j++) {
                    pointSet[index] = new int[3];
                    pointSet[index][0] = interpolateArray[0][j];
                    pointSet[index][1] = interpolateArray[1][j];
                    pointSet[index][2] = linePoints[i][2];
                    index++;
                }
            }
            pointSet[index] = new int[3];
            pointSet[index][0] = linePoints[i][0];
            pointSet[index][1] = linePoints[i][1];
            pointSet[index][2] = linePoints[i][2];
            index++;
        }
        int[][] newData;
        newData = Arrays.copyOfRange(pointSet, 0, index);
        //Log.v(TAG,"The length of newData is " + newData.length);
        return newData;
    }

    public int callSmoothDistance(int newX, int newY, int newPId) {
        // 5: no update; 4: shortdistance;
        // 0: interpolate 0; 1: interpolate 1 3: interpolate 3
        if (Pts == 3) { // update
            _x0 = xAxes[0];
            xAxes[0] = xAxes[1];
            xAxes[1] = xAxes[2];
            xAxes[2] = newX;

            _y0 = yAxes[0];
            yAxes[0] = yAxes[1];
            yAxes[1] = yAxes[2];
            yAxes[2] = newY;

            ptnIds[0] = ptnIds[1];
            ptnIds[1] = ptnIds[2];
            ptnIds[2] = newPId;
            PtsTrue = 4;

            if (ptnIds[2] == ptnIds[1]) {
                currentDist = (xAxes[2] - xAxes[1]) * (xAxes[2] - xAxes[1]) + (yAxes[2] - yAxes[1]) * (yAxes[2] - yAxes[1]);
                if (currentDist < maxDist) {
                    smoothShortDistance();
                    return 4;
                } else {
                    smoothLongDistance();
                    return interpolateNum;
                }
            } else {
                reset();
                xAxes[Pts] = newX;
                yAxes[Pts] = newY;
                ptnIds[Pts] = newPId;
                Pts++;
                return 5;
            }

        } else if (Pts == 2) {
            xAxes[Pts] = newX;
            yAxes[Pts] = newY;
            ptnIds[Pts] = newPId;
            Pts++;

            if (ptnIds[Pts - 1] == ptnIds[Pts - 2]) {
                currentDist = (xAxes[2] - xAxes[1]) * (xAxes[2] - xAxes[1]) + (yAxes[2] - yAxes[1]) * (yAxes[2] - yAxes[1]);
                if (currentDist < maxDist) {
                    double value = Math.sqrt((xAxes[1] - xAxes[0]) * (xAxes[1] - xAxes[0]) + (yAxes[1] - yAxes[0]) * (yAxes[1] - yAxes[0]));
                    DirectX = (xAxes[1] - xAxes[0]) / value;
                    DirectY = (yAxes[1] - yAxes[0]) / value;
                    smoothShortDistance();
                    return 4;
                } else {
                    smoothLongDistance();
                    return interpolateNum;
                }
            } else {
                reset();
                xAxes[Pts] = newX;
                yAxes[Pts] = newY;
                ptnIds[Pts] = newPId;
                Pts++;
                return 5;
            }
        } else { // add
            if (Pts == 1 && ptnIds[Pts - 1] != newPId) {
                reset();
            }
            xAxes[Pts] = newX;
            yAxes[Pts] = newY;
            ptnIds[Pts] = newPId;
            Pts++;
            return 5;
        }
    }

    private void smoothShortDistance() {
        double pointDistance; //^2? the distance between x2 and x1
        double detx, dety; // the vetor from x1 to x2
        double prox, proy;
        double alpha;
        double tmp;

        pointDistance = (xAxes[2] - xAxes[1]) * (xAxes[2] - xAxes[1]) + (yAxes[2] - yAxes[1]) * (yAxes[2] - yAxes[1]);
        if (pointDistance < minDist) {
            xAxes[2] = xAxes[1];
            yAxes[2] = yAxes[1];
        } else if (DirectX > 1) { // impossible
            DirectX = xAxes[2] - xAxes[1];
            DirectY = yAxes[2] - yAxes[1];
            tmp = Math.sqrt(DirectX * DirectX + DirectY * DirectY);

            DirectX = DirectX / tmp;
            DirectY = DirectY / tmp;
        } else {
            detx = xAxes[2] - xAxes[1];
            dety = yAxes[2] - yAxes[1];

            tmp = detx * DirectX + dety * DirectY;
            prox = tmp * DirectX - detx;
            proy = tmp * DirectY - dety;

            alpha = Math.sqrt(minDist / pointDistance);
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

            xAxes[2] = xAxes[2] + (int) (prox * tmp);
            yAxes[2] = yAxes[2] + (int) (proy * tmp);

            DirectX = xAxes[2] - xAxes[1];
            DirectY = yAxes[2] - yAxes[1];

            tmp = Math.sqrt(DirectX * DirectX + DirectY * DirectY);

            if (tmp != 0.0) {
                DirectX /= tmp;
                DirectY /= tmp;
            }
        }
    }

    private void smoothLongDistance() {
        int a0, a1, a2, b0, b1, b2, t;
        int c0, c1, c2, xx, yy;
        int tmpPoints;
        int i;
        if (Pts > 1) {
            xx = (xAxes[2] - xAxes[1]) * (xAxes[2] - xAxes[1]);
            yy = (yAxes[2] - yAxes[1]) * (yAxes[2] - yAxes[1]);
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
//                c0 = (int)(_x0 + xAxes[0] + xAxes[1] +xAxes[2]);
//                c1 = (int)(_x0 + 0*xAxes[0] + xAxes[1] +2*xAxes[2]);
//                c2 = (int)(_x0 + 0*xAxes[0] + xAxes[1] +4*xAxes[2]);
//                a0 = (int)(0.55*c0 + 0.15*c1 - 0.25*c2);
//                a1 = (int)(0.15*c0 + 0.45*c1 - 0.25*c2);
//                a2 = (int)(-0.25*c0 - 0.25*c1 + 0.25*c2);
//                xx = a0 + a1 + a2;
//                c0 = (int)(_y0 + yAxes[0] + yAxes[1] +yAxes[2]);
//                c1 = (int)(_y0 + 0*yAxes[0] + yAxes[1] +2*yAxes[2]);
//                c2 = (int)(_y0 + 0*yAxes[0] + yAxes[1] +4*yAxes[2]);
//                b0 = (int)(0.55*c0 + 0.15*c1 - 0.25*c2);
//                b1 = (int)(0.15*c0 + 0.45*c1 - 0.25*c2);
//                b2 = (int)(-0.25*c0 - 0.25*c1 + 0.25*c2);
//                yy = b0 + b1 + b2;
//                xAxes[1] = xx;
//                yAxes[1] = yy;
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
                a0 = xAxes[1];
                a1 = (xAxes[2] - xAxes[0]) / 2;
                a2 = (xAxes[0] + xAxes[2]) / 2 - xAxes[1];
                b0 = yAxes[1];
                b1 = (yAxes[2] - yAxes[0]) / 2;
                b2 = (yAxes[0] + yAxes[2]) / 2 - yAxes[1];
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

    private void reset() {
        xAxes[0] = xAxes[1] = xAxes[2] = 0;
        yAxes[0] = yAxes[1] = yAxes[2] = 0;
        ptnIds[0] = ptnIds[1] = ptnIds[2] = -1;
        Pts = 0;
        DirectX = DirectY = 0;
        interpolateNum = 0;
        PtsTrue = 0;
    }
}
