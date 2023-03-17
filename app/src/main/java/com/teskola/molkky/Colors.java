package com.teskola.molkky;

public class Colors
{
    public static int selectBackground(Player player, boolean onlyGray) {
        final int GOLD = R.drawable.gold_background;
        final int PURPLE = R.drawable.purple_background;
        final int GRAY = R.drawable.gray_background;
        final int GREEN = R.drawable.green_background;
        final int GREEN_YELLOW = R.drawable.green_yellow_background;
        final int GREEN_ORANGE = R.drawable.green_orange_background;
        final int YELLOW = R.drawable.yellow_background;
        final int ORANGE = R.drawable.orange_background;
        final int BEIGE = R.drawable.beige_white_background;

        if (player.isEliminated())
            return GRAY;
        if (player.countAll() == 50)
            return GOLD;

        int size = player.getTosses().size();
        if (!onlyGray) {
            int misses = 0;
            if (size > 1 && player.getToss(size - 1) == 0 && player.getToss(size - 2) == 0)
                misses = 2;
            else if (size > 0 && player.getToss(size - 1) == 0)
                misses = 1;
            if (player.excessesTargetPoints(size - 1) == 1)
                return PURPLE;
            if (player.pointsToWin() == 0) {
                if (misses == 2)
                    return ORANGE;
                if (misses == 1)
                    return YELLOW;
                else
                    return BEIGE;
            } else {
                if (misses == 2)
                    return GREEN_ORANGE;
                if (misses == 1)
                    return GREEN_YELLOW;
            }
            return GREEN;
        }
        return BEIGE;
    }
}
