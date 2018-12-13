package org.AIandGames.mancalabot.helpers;

import org.AIandGames.mancalabot.Enums.Side;
import org.AIandGames.mancalabot.GameTreeNode;
import org.AIandGames.mancalabot.GeneratorRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

public class TreeGenerator implements Runnable {
    private static final int SINGLE_THREAD_DEPTH = 3;
    private BlockingDeque<Runnable> threadedQueue;
    private ExecutorService threadPool;
    private GameTreeNode rootNode;
    private int overallDepth;
    private Side ourSide;

    public TreeGenerator(final GameTreeNode rootNode, final int overallDepth, final Side ourSide) {
        this.rootNode = rootNode;
        this.overallDepth = overallDepth;
        this.ourSide = ourSide;

        this.threadedQueue = new LinkedBlockingDeque<>();
        final int logicalCores = Runtime.getRuntime().availableProcessors();
        this.threadPool = new ThreadPoolExecutor(logicalCores, logicalCores, 10000L, TimeUnit.SECONDS, this.threadedQueue);
    }

    public TreeGenerator() {
    }

    @Override
    public void run() {
        try {
            final int depthPerThread = this.overallDepth - SINGLE_THREAD_DEPTH;

            final ArrayList<Runnable> runnables = new ArrayList<>();
            final List<GameTreeNode> leafNodesToRunThreaded = new ArrayList<>();

            this.rootNode.generateChildren(2, this.ourSide);

            this.rootNode.getChildren().stream()
                    .filter(Objects::nonNull)
                    .forEach(childNode -> leafNodesToRunThreaded.addAll(childNode.getChildren()));

            leafNodesToRunThreaded.stream()
                    .filter(Objects::nonNull)
                    .forEach(node -> runnables.add(new GeneratorRunnable(node, depthPerThread, this.ourSide)));

            runnables.forEach(this.threadPool::submit);


            while (this.threadedQueue.size() > 0) {
            } // nasty but works

            this.threadPool.shutdown();

        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public GameTreeNode getRootNode() {
        return this.rootNode;
    }
}
