package com.tannuo.note.whiteboard;

/**
 * Created by Allen on 16-07-21.
 */
public class BezierSmooth {
    private final int SEGMENT_COUNT = 5;
    private final int AVERAGE_DISTANCE = 44000;
    static int ID;
    static int[] p0, p1, p2, p3, p4, midpoint, lastpoint;
    int[][] pointbuffer;
    int[][] lastnotweakpointbuffer;
    int[][] lastpointbuffer;
    boolean isNewStroke;
    private int[] lpfilter;
    private static int counter;
    int[][] pointsum;
    int[][] pointstemp;
    int[][] temp;
    int bufferlength;
    int lastbufferlength;
    int strokepoints;
    int[] lastaveragepoint;
    int lastnotweakpointbufferlength;
    boolean doInterpolation;
    int index;
    int recordnum;


    public BezierSmooth() {
        p0 = null;
        p1 = null;
        p2 = null;
        p3 = null;
        p4 = null;
        midpoint = null;
        lastpoint = null;
        lpfilter = null;
        ID = -1;
        counter = 0;
        pointbuffer = new int[50][3];
        lastnotweakpointbuffer = new int[50][3];
        lastpointbuffer = new int[50][3];
        pointsum = new int[50][5];
        pointstemp = new int[SEGMENT_COUNT + 1][3];
        temp = new int[20][6];
        pointbuffer = new int[50][3];
        bufferlength = 1000;
        isNewStroke = false;
        lastbufferlength = 0;
        strokepoints = 0;
        lastaveragepoint = new int[6];
        lastnotweakpointbufferlength = 0;
        doInterpolation = false;
        index = 0;
        recordnum = 0;
    }

    private int CalculateDistance(int[] p1, int[] p2) {
        if (p1[2] == p2[2]) {
            int x = p2[0] - p1[0];
            int y = Math.round((p2[1] - p1[1]) * 9 / 16);
            return x * x + y * y;
        } else {
            return -1;
        }
    }

    private double CalculateAngle(int[] p2, int[] p1, int[] p3) {
        int p1y = p1[1] * 9 / 16;
        int p2y = p2[1] * 9 / 16;
        int p3y = p3[1] * 9 / 16;
        double p12 = Math.sqrt(Math.pow((p1[0] - p2[0]), 2) + Math.pow((p1y - p2y), 2));
        double p13 = Math.sqrt(Math.pow((p1[0] - p3[0]), 2) + Math.pow((p1y - p3y), 2));
        double p23p23 = Math.pow((p2[0] - p3[0]), 2) + Math.pow((p2y - p3y), 2);
        float temp = (float) ((p12 * p12) + (p13 * p13) - p23p23) / (float) (2 * p12 * p13);
        if (temp < -1) {
            temp = -1;
        }

        double radians = Math.acos(temp);
        double x = (radians / Math.PI) * 180;

        return x;
    }

    private int[] CalculateAverage(int[][] pointbuffer, int counter) {
        int sumx = 0;
        int sumy = 0;
        int[] averagepoint = new int[6];
        if (counter < 1) {
            averagepoint = null;
            return averagepoint;
        } else if (counter == 1) {
            averagepoint[0] = pointbuffer[0][0];
            averagepoint[1] = pointbuffer[0][1];
            averagepoint[2] = pointbuffer[0][2];
            averagepoint[3] = 0;
            averagepoint[4] = 0;
            return averagepoint;
        }
        for (int i = 0; i < counter; i++) {
            sumx += pointbuffer[i][0];
            sumy += pointbuffer[i][1];
        }
        averagepoint[0] = sumx / counter;
        averagepoint[1] = sumy / counter;
        averagepoint[2] = pointbuffer[0][2];
        averagepoint[3] = 0;
        averagepoint[4] = 0;
        return averagepoint;
    }

    private int[] CalculateQuadraticBezierPoint(float t, int[] p0, int[] p1, int[] p2) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float[] p = new float[3];
        int[] q = new int[3];
        p[0] = (float) (uu * (float) p0[0]);
        p[1] = (float) (uu * (float) p0[1]);
        p[0] += 2 * u * t * (float) p1[0];
        p[1] += 2 * u * t * (float) p1[1];
        p[0] += tt * (float) p2[0];
        p[1] += tt * (float) p2[1];
        p[2] = (float) p0[2];
        q[0] = (int) p[0];
        q[1] = (int) p[1];
        q[2] = (int) p[2];
        return q;
    }

    private int[][] InterpolateQuadraticBezierPoint(int SEGMENT_COUNT, int[] p0, int[] p1, int[] p2) {
        int[] q0;
        int[] q1;
        int[][] points = new int[(SEGMENT_COUNT + 1)][3];
        q0 = CalculateQuadraticBezierPoint(0, p0, p1, p2);
        points[0] = q0;
        for (int i = 1; i < SEGMENT_COUNT + 1; i++) {
            float t = (float) i / (float) (SEGMENT_COUNT);
            q1 = CalculateQuadraticBezierPoint(t, p0, p1, p2);
            points[i] = q1;
        }
        return points;
    }

    private int[] CalculateBezierPoint(float t, int[] p0, int[] p1, int[] p2, int[] p3) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        float[] p = new float[3];
        int[] q = new int[3];
        p[0] = (float) (uuu * (float) p0[0]);
        p[1] = (float) (uuu * (float) p0[1]);
        p[0] += (float) (3 * uu * t * (float) p1[0]);
        p[1] += (float) (3 * uu * t * (float) p1[1]);
        p[0] += (float) (3 * u * tt * (float) p2[0]);
        p[1] += (float) (3 * u * tt * (float) p2[1]);
        p[0] += (float) (ttt * (float) p3[0]);
        p[1] += (float) (ttt * (float) p3[1]);
        p[2] = p0[2];
        q[0] = (int) p[0];
        q[1] = (int) p[1];
        q[2] = (int) p[2];
        return q;
    }

    private int[][] InterpolatePoint(int SEGMENT_COUNT, int[] p0, int[] p1, int[] p2, int[] p3) {
        int[] q0;
        int[] q1;
        int[][] points = new int[(SEGMENT_COUNT + 1)][3];
        q0 = CalculateBezierPoint(0, p0, p1, p2, p3);
        points[0] = q0;
        for (int i = 1; i < SEGMENT_COUNT + 1; i++) {
            float t = (float) i / (float) (SEGMENT_COUNT);
            q1 = CalculateBezierPoint(t, p0, p1, p2, p3);
            points[i] = q1;
        }
        return points;
    }


    /* point[4] :  Point Status Classification     1 = isCritical  2 = isControl  3 = isWeak  4 = isGarbage 0 = default*/
    /*
       Point Filter Algorithm
     */
    public int[][] Point_Filter(int[] points, boolean ReadytoUpdate) {
//        int[] points = new int[4];
//        points[0] = p.pointX;
//        points[1] = p.pointY;
//        points[2] = p.pointId;
//        Log.v("Points Input", " X " + points[0] + " Y " + points[1] + " ID " + points[2]);
        int[] averagepoint;
        boolean update = false;
        if (lpfilter == null) {
            lpfilter = points;
//            pointbuffer[counter] = points;
//            if (counter <= 48) {
//                counter++;
//            }
        } else {
//            Log.v("lpfilter", "lpfilter ID = " + lpfilter[2]);
            if (points[2] == lpfilter[2]) {
                int distance = CalculateDistance(lpfilter, points);
                if (distance < AVERAGE_DISTANCE && distance >= 0) {
                    pointbuffer[counter] = points;
                    if (counter <= 48) {
                        counter++;
                    }
                } else if (distance >= AVERAGE_DISTANCE) {
                    bufferlength = counter;
                    //                for(int o = 0; o < bufferlength; o++){
                    //                    Log.v("pointbuffer", "x = "+ pointbuffer[o][0] + "y = "+ pointbuffer[o][1] + "ID = "+ pointbuffer[o][2]);
                    //                }
                    averagepoint = CalculateAverage(pointbuffer, counter);
                    if (averagepoint != null) {
                        averagepoint[3] = bufferlength;
                        if (bufferlength >= 10 && bufferlength >= 3 * lastbufferlength && strokepoints != 0) {
                            averagepoint[4] = 1;
                        } else if (bufferlength >= 10 && bufferlength >= (lastbufferlength - 3) && strokepoints != 0 && lastaveragepoint[4] == 1) {
                            averagepoint[4] = 1;
                        } else if (bufferlength >= 10 && bufferlength >= 2 * lastbufferlength && strokepoints != 0) {
                            averagepoint[4] = 2;
                        } else if (bufferlength <= 1) {
                            averagepoint[4] = 3;
                            if (bufferlength == 1) {
                                averagepoint[5] = 1;
                            }
                        }
                        System.arraycopy(averagepoint, 0, temp[index], 0, 6);
                        index++;
                        strokepoints++;
                        isNewStroke = false;
                        if (averagepoint[4] != 3) {
                            for (int m = 0; m < counter; m++) {
                                for (int n = 0; n < 3; n++) {
                                    lastnotweakpointbuffer[m][n] = pointbuffer[m][n];
                                }
                            }
                            lastnotweakpointbufferlength = counter;
                        }
                        for (int m = 0; m <= 5; m++) {
                            lastaveragepoint[m] = averagepoint[m];
                        }
                    }
                    for (int m = 0; m < counter; m++) {
                        for (int n = 0; n < 3; n++) {
                            lastpointbuffer[m][n] = pointbuffer[m][n];
                        }
                    }
                    lastbufferlength = bufferlength;
                    counter = 0;
                    pointbuffer[counter] = points;
                    if (counter <= 48) {
                        counter++;
                    }
                    lpfilter = points;
                }
            }
        }
        if ((ReadytoUpdate && points[2] == lpfilter[2])||points[2] != lpfilter[2]) {
            update = true;
//            Log.v("Debug", "Enter");
            bufferlength = counter;
            if (strokepoints == 0 && bufferlength >= 2) {
                if (CalculateDistance(pointbuffer[0], pointbuffer[bufferlength - 1]) <= 2500) {
                    pointbuffer[0][0] = pointbuffer[0][0] - 50;
                    pointbuffer[bufferlength - 1][0] = pointbuffer[bufferlength - 1][0] + 50;
                }
                System.arraycopy(pointbuffer[0], 0, temp[index], 0, 3);
                temp[index][3] = 0;
                temp[index][4] = 0;
                temp[index][5] = 0;
                index++;
                System.arraycopy(pointbuffer[bufferlength - 1], 0, temp[index], 0, 3);
                temp[index][3] = 0;
                temp[index][4] = 0;
                temp[index][5] = 0;
                index++;
            } else if (strokepoints == 1 && bufferlength <= 2 && lastbufferlength >= 2) {
                if (index > 0) {
                    index--;
                }
                if (CalculateDistance(lastpointbuffer[0], lastpointbuffer[lastbufferlength - 1]) <= 2500) {
                    lastpointbuffer[0][0] = lastpointbuffer[0][0] - 50;
                    lastpointbuffer[lastbufferlength - 1][0] = lastpointbuffer[lastbufferlength - 1][0] + 50;
                }
                System.arraycopy(lastpointbuffer[0], 0, temp[index], 0, 3);
                temp[index][3] = 0;
                temp[index][4] = 0;
                temp[index][5] = 0;
                index++;
                System.arraycopy(lastpointbuffer[lastbufferlength - 1], 0, temp[index], 0, 3);
                temp[index][3] = 0;
                temp[index][4] = 0;
                temp[index][5] = 0;
                index++;
            } else {
                averagepoint = CalculateAverage(pointbuffer, counter);
                if (averagepoint != null && lastaveragepoint != null) {
                    averagepoint[3] = bufferlength;
                    if (bufferlength <= 1) {
                        int length = index - 1;
                        int record = 0;
                        while (length >= 0 && record <= 2) {
                            if (temp[length][4] == 3 || temp[length][5] == 1) {
                                index--;
                                strokepoints--;
                                record++;
                            } else {
                                break;
                            }
                            length--;
                        }
                        if (strokepoints == 1) {
                            if (index > 0) {
                                index--;
                            }
                            if (CalculateDistance(lastnotweakpointbuffer[0], lastnotweakpointbuffer[lastnotweakpointbufferlength - 1]) <= 2500) {
                                lastnotweakpointbuffer[0][0] -= 50;
                                lastnotweakpointbuffer[lastnotweakpointbufferlength - 1][0] = lastnotweakpointbuffer[lastnotweakpointbufferlength - 1][0] + 50;
                            }
                            System.arraycopy(lastnotweakpointbuffer[0], 0, temp[index], 0, 3);
                            temp[index][3] = 0;
                            temp[index][4] = 0;
                            temp[index][5] = 0;
                            index++;
                            System.arraycopy(lastnotweakpointbuffer[lastnotweakpointbufferlength - 1], 0, temp[index], 0, 3);
                            temp[index][3] = 0;
                            temp[index][4] = 0;
                            temp[index][5] = 0;
                            index++;
                        }
                    } else {
                        if ((bufferlength >= 4 || bufferlength >= lastbufferlength) && CalculateDistance(lastaveragepoint, averagepoint) <= 4 * AVERAGE_DISTANCE) {
                            System.arraycopy(averagepoint, 0, temp[index], 0, 6);
                            index++;
                        } else {
                            if (strokepoints == 1) {
                                if (index > 0) {
                                    index--;
                                }
                                if (CalculateDistance(lastpointbuffer[0], lastpointbuffer[lastbufferlength - 1]) <= 2500) {
                                    lastpointbuffer[0][0] = lastpointbuffer[0][0] - 50;
                                    System.arraycopy(lastpointbuffer[0], 0, temp[index], 0, 3);
                                    temp[index][3] = 0;
                                    temp[index][4] = 0;
                                    temp[index][5] = 0;
                                    index++;
                                    if (lastbufferlength > 1) {
                                        lastpointbuffer[lastbufferlength - 1][0] = lastpointbuffer[lastbufferlength - 1][0] + 50;
                                        System.arraycopy(lastpointbuffer[lastbufferlength - 1], 0, temp[index], 0, 3);
                                        temp[index][3] = 0;
                                        temp[index][4] = 0;
                                        temp[index][5] = 0;
                                        index++;
                                    } else {
                                        lastpointbuffer[0][0] = lastpointbuffer[0][0] + 100;
                                        System.arraycopy(lastpointbuffer[0], 0, temp[index], 0, 3);
                                        temp[index][3] = 0;
                                        temp[index][4] = 0;
                                        temp[index][5] = 0;
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            lastbufferlength = 0;
            counter = 0;
            lpfilter = null;
//            if(ReadytoUpdate && p.pointId == lpfilter[2]) {
//                lpfilter = null;
//            }else{
//                lpfilter = points;
//                pointbuffer[counter] = points;
//                if (counter <= 48) {
//                    counter++;
//                }
//            }
            strokepoints = 0;
            isNewStroke = true;
        }
        if ((index >= 2 && temp[index - 1][4] != 3) || index >= 4 || update) {
//            Log.v("Debug","Enter2");
//            Log.v("Index","Index = " + index);
            int[][] newpoints = new int[index][];
            for (int k = 0; k < index; k++) {
                int[] aMatrix = temp[k];
                int aLength = aMatrix.length;
                newpoints[k] = new int[aLength];
                System.arraycopy(aMatrix, 0, newpoints[k], 0, aLength);
            }
            index = 0;
            return BezierSmooth(newpoints, update);
        } else {
            return null;
        }
    }

    /*
       Stroke Smooth Algorithm
     */
    private int[][] BezierSmooth(int[][] points, boolean ReadytoUpdate) {

        int[][] newData;
        int index = 0;
        if (points != null) {
            for (int i = 0; i < points.length; i++) {
//                Log.v("Beizer Input", " X " + points[i][0] + " Y " + points[i][1] + " ID " + points[i][2]);
                if (ID == -1) {
                    ID = points[i][2];
                }
                if (lastpoint == null) {
                    lastpoint = points[0];
                }
                if (ID == points[i][2]) {
                    if (lastpoint[4] == 2) {
                        doInterpolation = true;
                    }
                    if (points[i][4] == 1) {
                        if ((p0 != null) && (p1 != null) && (p2 != null) && (p3 != null)) {
                            p3[1] = (p2[1] + points[i][1]) / 2;
                            p3[0] = (p2[0] + points[i][0]) / 2;
                            pointstemp = InterpolatePoint(SEGMENT_COUNT, p0, p1, p2, p3);
                            for (int m = 0; m <= SEGMENT_COUNT; m++) {
                                for (int n = 0; n < 3; n++) {
                                    pointsum[index + m][n] = pointstemp[m][n];
                                }
                            }
                            index += SEGMENT_COUNT + 1;
                            pointsum[index] = p3;
                            pointsum[index + 1] = points[i];
                            index += 2;
                        } else if ((p0 != null) && (p1 != null) && (p2 != null)) {
                            pointstemp = InterpolatePoint(SEGMENT_COUNT, p0, p1, p2, points[i]);
                            for (int m = 0; m <= SEGMENT_COUNT; m++) {
                                for (int n = 0; n < 3; n++) {
                                    pointsum[index + m][n] = pointstemp[m][n];
                                }
                            }
                            index += SEGMENT_COUNT + 1;
                        } else if ((p0 != null) && (p1 != null)) {
                            pointstemp = InterpolateQuadraticBezierPoint(SEGMENT_COUNT, p0, p1, points[i]);
                            for (int m = 0; m <= SEGMENT_COUNT; m++) {
                                for (int n = 0; n < 3; n++) {
                                    pointsum[index + m][n] = pointstemp[m][n];
                                }
                            }
                            index += SEGMENT_COUNT + 1;
                        } else if (p0 != null) {
                            pointsum[index] = p0;
                            pointsum[index + 1] = points[i];
                            index += 2;
                        }
                        ID = points[i][2];
                        p0 = points[i];
                        p1 = null;
                        p2 = null;
                        p3 = null;
                    } else {
                        if (p0 == null) {
                            p0 = points[i];
                        } else if (p1 == null) {
                            p1 = points[i];
                        } else if (p2 == null) {
                            p2 = points[i];
                            p3 = null;
                            if (doInterpolation) {
                                if (CalculateAngle(p0, p1, p2) <= 50) {
                                    for (int m = 0; m < 5; m++) {
                                        pointsum[index][m] = p0[m];
                                        pointsum[index + 1][m] = p1[m];
                                    }
                                    index += 2;
                                    p0 = p1;
                                    p1 = p2;
                                    p2 = null;
                                    p3 = null;
                                    p4 = null;
                                } else {
//                                Log.v("Angle1", " Angle = " + CalculateAngle(p0,p1,p2));

                                    p1[0] = (int) ((p1[0] - p0[0] + p1[0] - p2[0]) / 1.5) + p1[0];
                                    p1[1] = (int) ((p1[1] - p0[1] + p1[1] - p2[1]) / 1.5) + p1[1];
                                }
                                doInterpolation = false;
                            }
                        } else if (p3 == null) {
                            p3 = points[i];
                            p4 = null;
                            if (doInterpolation) {
                                if (CalculateAngle(p1, p2, p3) <= 50) {
                                    pointstemp = InterpolateQuadraticBezierPoint(SEGMENT_COUNT, p0, p1, p2);
                                    for (int m = 0; m <= SEGMENT_COUNT; m++) {
                                        for (int n = 0; n < 3; n++) {
                                            pointsum[index + m][n] = pointstemp[m][n];
                                        }
                                    }
                                    index += SEGMENT_COUNT + 1;
                                    p0 = p2;
                                    p1 = p3;
                                    p2 = null;
                                    p3 = null;
                                    p4 = null;
                                } else {
                                    p2[0] = (int) ((p2[0] - p1[0] + p2[0] - p3[0]) / 1.5) + p2[0];
                                    p2[1] = (int) ((p2[1] - p1[1] + p2[1] - p3[1]) / 1.5) + p2[1];
                                }
                                doInterpolation = false;
                            }
                        } else if (p4 == null) {
                            p4 = points[i];
                            int midpointx = (p2[0] + p4[0]) / 2;
                            int midpointy = (p2[1] + p4[1]) / 2;
                            if (doInterpolation != true) {
                                p3[0] = midpointx;
                                p3[1] = midpointy;
                            } else {
                                if (CalculateAngle(p2, p3, p4) >= 50) {
//                                Log.v("Angle3", " Angle = " + CalculateAngle(p2,p3,p4));
                                    p3[0] = (p3[0] - midpointx) / 2 + midpointx;
                                    p3[1] = (p3[1] - midpointy) / 2 + midpointy;
                                }
                                doInterpolation = false;
                            }
                            pointstemp = InterpolatePoint(SEGMENT_COUNT, p0, p1, p2, p3);
                            for (int m = 0; m <= SEGMENT_COUNT; m++) {
                                for (int n = 0; n < 3; n++) {
                                    pointsum[index + m][n] = pointstemp[m][n];
                                }
                            }
                            index += SEGMENT_COUNT + 1;
                            p0 = p3;
                            p1 = p4;
                            p2 = null;
                            p3 = null;
                            p4 = null;
                        }
                    }
                }
                lastpoint = points[i];
            }
        }
        if (ReadytoUpdate) {
            if ((p0 != null) && (p1 != null) && (p2 != null) && (p3 != null)) {
                pointstemp = InterpolatePoint(SEGMENT_COUNT, p0, p1, p2, p3);
                for (int m = 0; m <= SEGMENT_COUNT; m++) {
                    for (int n = 0; n < 3; n++) {
                        pointsum[index + m][n] = pointstemp[m][n];
                    }
                }
                index += SEGMENT_COUNT + 1;
            } else if ((p0 != null) && (p1 != null) && (p2 != null)) {
                pointstemp = InterpolateQuadraticBezierPoint(SEGMENT_COUNT, p0, p1, p2);
                for (int m = 0; m <= SEGMENT_COUNT; m++) {
                    for (int n = 0; n < 3; n++) {
                        pointsum[index + m][n] = pointstemp[m][n];
                    }
                }
                index += SEGMENT_COUNT + 1;
            } else if ((p0 != null) && (p1 != null)) {
                pointsum[index] = p0;
                pointsum[index + 1] = p1;
                index += 2;
            }
            ID = -1;
            p0 = null;
            p1 = null;
            p2 = null;
            p3 = null;
            p4 = null;
            lastpoint = null;
        }
        if (index != 0) {
            newData = new int[index][3];
            for (int k = 0; k < index; k++) {
                for (int j = 0; j < 3; j++) {
                    newData[k][j] = pointsum[k][j];
                }
//                Log.v("Beizer Output", " X " + newData[k][0] + " Y " + newData[k][1] + " ID " + newData[k][2]);
            }
        } else {
            newData = null;
        }
        return newData;
    }
}

