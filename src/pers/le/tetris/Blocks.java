package pers.le.tetris;

public class Blocks {

    public static final boolean[][][] Shape = {
        // * * * *
        {
            {true, true, true, true}
        },
        // *
        // * * *
        {
            {true, false, false},
            {true, true, true}
        },
        //     *
        // * * *
        {
            {false, false, true},
            {true, true, true}
        },
        // * *
        // * *
        {
            {true, true},
            {true, true}
        },
        //   * *
        // * *
        {
            {false, true, true},
            {true, true, false}
        },
        //   *
        // * * *
        {
            {false, true, false},
            {true, true, true}
        },
        // * *
        //   * *
        {
            {true, true, false},
            {false, true, true}
        }
    };

}
