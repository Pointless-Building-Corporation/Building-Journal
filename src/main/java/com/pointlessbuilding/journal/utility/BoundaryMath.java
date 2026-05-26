package com.pointlessbuilding.journal.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class BoundaryMath {
    
    // This uses Klee's Algorithm to solve for the Klee's Measure Problem. n=50 is the max number of boxes which necessitates the O(n^(3-1) log n) solution.
    public static long unionVolume(List<int[]> firsts, List<int[]> seconds) {

        List<int[]> mins = new ArrayList<>();
        List<int[]> maxs = new ArrayList<>();
        for(int i = 0; i < firsts.size(); i++) {
            int[] f = firsts.get(i);
            int[] s = seconds.get(i);
            mins.add(new int[]{Math.min(f[0],s[0]), Math.min(f[1], s[1]), Math.min(f[2],s[2])});
            maxs.add(new int[]{Math.max(f[0],s[0])+1, Math.max(f[1], s[1])+1, Math.max(f[2],s[2])+1});
        }

        TreeSet<Integer> xSet = new TreeSet<>();    // Naturally orders the mins and maxs
        for(int i = 0; i < mins.size(); i++) {
            xSet.add(mins.get(i)[0]);
            xSet.add(maxs.get(i)[0]);
        }
        Integer[] xs = xSet.toArray(new Integer[0]);

        long total = 0;
        for(int xi = 0; xi < xs.length -1; xi++) {
            int x0 = xs[xi], x1 = xs[xi+1];
            List<int[]> activeMins = new ArrayList<>();
            List<int[]> activeMaxs = new ArrayList<>();
            for(int i = 0; i < mins.size(); i++) {
                if (mins.get(i)[0] <= x0 && maxs.get(i)[0] >= x1) {
                    activeMins.add(mins.get(i));
                    activeMaxs.add(maxs.get(i));
                }
            }
            total += (long)(x1-x0) * sweepY(activeMins, activeMaxs);
        }

        return total;
    }

    // This is a more specific sweepY implement.
    public static List<int[]> mergeYIntervals(int x, int z, List<int[]> mins,  List<int[]> maxs) {
        TreeSet<Integer> ySet = new TreeSet<>();
        for (int i = 0; i < mins.size(); i++) {
            if (x >= mins.get(i)[0] && x <= maxs.get(i)[0] &&
                z >= mins.get(i)[2] && z <= maxs.get(i)[2]) {
                ySet.add(mins.get(i)[1]);
                ySet.add(maxs.get(i)[1] + 1);
            }
        }
        if (ySet.isEmpty()) return List.of();

        Integer[] ys = ySet.toArray(new Integer[0]);
        List<int[]> result = new ArrayList<>();
        for (int yi = 0; yi < ys.length - 1; yi++) {
            int y0 = ys[yi], y1 = ys[yi + 1] - 1;
            // Check if this interval is covered by any box
            for (int i = 0; i < mins.size(); i++) {
                if (x >= mins.get(i)[0] && x <= maxs.get(i)[0] &&
                    z >= mins.get(i)[2] && z <= maxs.get(i)[2] &&
                    y0 >= mins.get(i)[1] && y1 <= maxs.get(i)[1]) {
                    result.add(new int[]{y0, y1});
                    break;
                }
            }
        }
        return result;
    }

    private static long sweepY(List<int[]> mins, List<int[]> maxs) {
        TreeSet<Integer> ySet = new TreeSet<>();
        for (int i = 0; i < mins.size(); i++) {
            ySet.add(mins.get(i)[1]);
            ySet.add(maxs.get(i)[1]);
        }
        Integer[] ys = ySet.toArray(new Integer[0]);

        long total = 0;
        for (int yi = 0; yi < ys.length - 1; yi++) {
            int y0 = ys[yi], y1 = ys[yi+1];
            List<int[]> activeMins = new ArrayList<>();
            List<int[]> activeMaxs = new ArrayList<>();
            for (int i = 0; i < mins.size(); i++) {
                if (mins.get(i)[1] <= y0 && maxs.get(i)[1] >= y1) {
                    activeMins.add(mins.get(i));
                    activeMaxs.add(maxs.get(i));
                }
            }
            total += (long)(y1 - y0) * sweepZ(activeMins, activeMaxs);
        }
        return total;
    }

    private static long sweepZ(List<int[]> mins, List<int[]> maxs) {
        TreeSet<Integer> zSet = new TreeSet<>();
        for (int i = 0; i < mins.size(); i++) {
            zSet.add(mins.get(i)[2]);
            zSet.add(maxs.get(i)[2]);
        }
        Integer[] zs = zSet.toArray(new Integer[0]);

        long total = 0;
        for (int zi = 0; zi < zs.length - 1; zi++) {
            int z0 = zs[zi], z1 = zs[zi+1];

            // Actual Klee's Algorithm heart; check if the current coord in list order is within the currently checked ordered coord
            boolean covered = false;
            for (int i = 0; i < mins.size(); i++) {
                if (mins.get(i)[2] <= z0 && maxs.get(i)[2] >= z1) {
                    covered = true;
                    break;
                }
            }
            if (covered) total += z1 - z0;
        }
        return total;
    }

}
