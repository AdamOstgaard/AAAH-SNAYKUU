package bot;

import gameLogic.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class TungurBottur implements Brain {
	private Random random = new Random();
	public static HashMap<String, HashMap<Direction, Integer>> memory = new HashMap<>();
	private static Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
	private static final int randomExplorationProbability = 10;

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState) {
		String stateHash = gameState.getBoard().getHash();
		HashMap<Direction, Integer> actions = memory.get(stateHash);

		//pick random dir and record result if statte is unknown
		if(actions == null){
			actions = new HashMap<Direction, Integer>();
			Direction dir = getRandomUnexplordedDirection(actions);
			memory.put(stateHash, actions);
			actions.put(dir, getActionScore(dir, yourSnake, gameState));
			return dir;
		}

		if(random.nextInt(100) < randomExplorationProbability) {
			Direction dir = getRandomUnexplordedDirection(actions);
			// dir is null if all actions are tested for this state.
			if (dir != null) {
				actions.put(dir, getActionScore(dir, yourSnake, gameState));
				return dir;
			}
		}

		return getBestDir(actions);
	}

	private Direction getBestDir(HashMap<Direction, Integer> knownDirs){
		int northScore = knownDirs.getOrDefault(Direction.NORTH, Integer.MIN_VALUE);
		int westScore = knownDirs.getOrDefault(Direction.NORTH, Integer.MIN_VALUE);
		int eastScore = knownDirs.getOrDefault(Direction.NORTH, Integer.MIN_VALUE);
		int southScore = knownDirs.getOrDefault(Direction.NORTH, Integer.MIN_VALUE);
		Integer[] values = {
				northScore,
				westScore,
				eastScore,
				southScore
		};
		int max = Collections.max(Arrays.asList(values));

		if(max == westScore){
			return Direction.WEST;
		}

		if(max == eastScore){
			return Direction.EAST;
		}

		if(max == southScore){
			return Direction.SOUTH;
		}

		return Direction.NORTH;
	}

	private Direction getRandomUnexplordedDirection(HashMap<Direction, Integer> explored){
		int startIndex = random.nextInt(4);
		for(int i = startIndex; i < directions.length + startIndex; i = (i+1)%4)
		{
			if(!explored.containsKey(directions[i])){
				return directions[i];
			}
		}

		return null;
	}

	private int getActionScore(Direction dir, Snake snake, GameState gameState){
		Position resultPosition = dir.calculateNextPosition(snake.getHeadPosition());

		if(!isValidDirection(snake.getCurrentDirection(), dir)){
			return Integer.MAX_VALUE;
		}

		if(gameState.getBoard().hasFruit(resultPosition)){
			return 10;
		}

		if(gameState.getBoard().hasWall(resultPosition)) {
			return -100;
		}

		return 0;
	}

	private boolean isValidDirection(Direction prevDir, Direction direction){
		switch (prevDir)
		{
			case NORTH:
				return (direction != Direction.SOUTH);

			case WEST:
				return (direction != Direction.EAST);

			case SOUTH:
				return (direction != Direction.NORTH);

			case EAST:
				return (direction != Direction.WEST);

			default:
				throw new IllegalArgumentException("No such Direction exists.");
		}
	}
}
