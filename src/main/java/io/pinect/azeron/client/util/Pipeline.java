package io.pinect.azeron.client.util;

import java.util.ArrayList;
import java.util.List;

public class Pipeline<I, O> {
    private List<Stage<I,O>> stages = new ArrayList<>();

    public Pipeline() {
    }

    public Pipeline<I,O> addStage(Stage<I,O> stage){
        stages.add(stage);
        return this;
    }

    public void run(I i, O o){
        for(Stage<I, O> stage: stages){
            if (!stage.process(i, o)) {
                break;
            }
        }
    }
}
