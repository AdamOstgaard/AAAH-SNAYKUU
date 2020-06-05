package bot;

import gameLogic.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.DeflaterInputStream;

public class TungurBottur implements Brain {
	private Random random = new Random();
	public static HashMap<String, HashMap<Direction, Integer>> memory = new HashMap<>();
	private static Direction[] directions = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
	private static final int randomExplorationProbability = 10;

	@Override
	public Direction getNextMove(Snake yourSnake, GameState gameState) {
		Direction selectedDir = Direction.NORTH;
		String stateHash = gameState.getBoard().getHash();
		try {

			HashMap<Direction, Integer> actions = memory.get(stateHash);

			//pick random dir and record result if statte is unknown
			if (actions == null) {
				actions = new HashMap<Direction, Integer>();
				Direction dir = getRandomUnexplordedDirection(actions);
				memory.put(stateHash, actions);
				actions.put(dir, getActionScore(dir, yourSnake, gameState));
				selectedDir = dir;
			}

			if (random.nextInt(100) < randomExplorationProbability) {
				Direction dir = getRandomUnexplordedDirection(actions);
				// dir is null if all actions are tested for this state.
				if (dir != null) {
					actions.put(dir, getActionScore(dir, yourSnake, gameState));
					selectedDir = dir;
				}
			}

			Direction currentDirection = yourSnake.getCurrentDirection();
			Direction nextDirection = Direction.NORTH;
			int score = Integer.MIN_VALUE;

			if (isValidDirection(Direction.WEST, currentDirection)) {
				ScoredDirection sd = lookAhead(8, Direction.WEST, new Snake(yourSnake), gameState.getBoard());
				if (sd.score > score) {
					score = sd.score;
					nextDirection = sd.direction;
				}
			}
			if (isValidDirection(Direction.EAST, currentDirection)) {
				ScoredDirection sd = lookAhead(8, Direction.WEST, new Snake(yourSnake), gameState.getBoard());
				if (sd.score > score) {
					score = sd.score;
					nextDirection = sd.direction;
				}
			}
			if (isValidDirection(Direction.SOUTH, currentDirection)) {
				ScoredDirection sd = lookAhead(8, Direction.WEST, new Snake(yourSnake), gameState.getBoard());
				if (sd.score > score) {
					score = sd.score;
					nextDirection = sd.direction;
				}
			}
			if (isValidDirection(Direction.NORTH, currentDirection)) {
				ScoredDirection sd = lookAhead(8, Direction.WEST, new Snake(yourSnake), gameState.getBoard());
				if (sd.score > score) {
					score = sd.score;
					nextDirection = sd.direction;
				}
			}
			System.out.println("known states: " + memory.size());
			System.out.println("score: " + score);

			selectedDir = nextDirection;
		}catch(Exception e){
			System.out.println(e.toString());
			e.printStackTrace();
		}
		HashMap<Direction, Integer> actions = memory.getOrDefault(stateHash, new HashMap<>());
		actions.put(selectedDir, getActionScore(selectedDir, yourSnake, gameState));
		memory.put(stateHash, actions);
		return selectedDir;
	}

	private ScoredDirection lookAhead(int iterations, Direction currentDirection, Snake snake , Board currentBoard){
		Position currentPosition = snake.getHeadPosition();

		if(!isOnMap(currentPosition, currentBoard)){
			return new ScoredDirection(Direction.NORTH, Integer.MIN_VALUE);
		}

		Board nextBoard = currentBoard.getBoardWithNextPosition(currentDirection, snake);

		int score = 0;
		if(iterations > 0) {
			int i = iterations - 1;
			if (isValidDirection(Direction.WEST, currentDirection)) {
				Position newHeadPosition = snake.moveHead(Direction.WEST);
				ScoredDirection sd = lookAhead(i, Direction.WEST, snake, nextBoard);
				if (sd.score > score) {
					score = sd.score;
				}
			}
			if (isValidDirection(Direction.EAST, currentDirection)) {
				Position newHeadPosition = snake.moveHead(Direction.EAST);
				ScoredDirection sd = lookAhead(i, Direction.EAST, snake, nextBoard);
				if (sd.score > score) {
					score = sd.score;
				}
			}
			if (isValidDirection(Direction.SOUTH, currentDirection)) {
				Position newHeadPosition = snake.moveHead(Direction.SOUTH);
				ScoredDirection sd = lookAhead(i, Direction.SOUTH, snake, nextBoard);
				if (sd.score > score) {
					score = sd.score;
				}
			}
			if (isValidDirection(Direction.NORTH, currentDirection)) {
				Position newHeadPosition = snake.moveHead(Direction.NORTH);
				ScoredDirection sd = lookAhead(i, Direction.NORTH, snake, nextBoard);
				if (sd.score > score) {
					score = sd.score;
				}
			}
		}

		HashMap<Direction, Integer> knownDirs = memory.get(nextBoard.getHash());
		if(knownDirs != null){
			ScoredDirection best = getBestDir(knownDirs);
			return new ScoredDirection(best.direction, best.score + score);
		}
		return new ScoredDirection(getRandomUnexplordedDirection(new HashMap<>()), -10);
	}

	private boolean isOnMap(Position pos, Board board){
		return pos.getY() < board.getWidth() && pos.getY() > 0 && pos.getX() < board.getHeight() && pos.getX() > 0;
	}

	private ScoredDirection getBestDir(HashMap<Direction, Integer> knownDirs){
		int northScore = knownDirs.getOrDefault(Direction.NORTH, -10);
		int westScore = knownDirs.getOrDefault(Direction.WEST, -10);
		int eastScore = knownDirs.getOrDefault(Direction.EAST, -10);
		int southScore = knownDirs.getOrDefault(Direction.SOUTH, -10);
		Integer[] values = {
				northScore,
				westScore,
				eastScore,
				southScore
		};
		int max = Collections.max(Arrays.asList(values));

		if(max == westScore){
			return new ScoredDirection(Direction.WEST, max);
		}

		if(max == eastScore){
			return new ScoredDirection(Direction.EAST, max);
		}

		if(max == southScore){
			return new ScoredDirection(Direction.SOUTH, max);
		}

		return new ScoredDirection(Direction.NORTH, max);
	}

	private Direction getRandomUnexplordedDirection(HashMap<Direction, Integer> explored){
		int startIndex = random.nextInt(4);
		for(int i = startIndex; i < directions.length + startIndex; i++)
		{
			if(!explored.containsKey(directions[i%4])){
				return directions[i%4];
			}
		}

		return null;
	}

	private int getActionScore(Direction dir, Snake snake, GameState gameState){
		Position resultPosition = dir.calculateNextPosition(snake.getHeadPosition());

		try {


			if (!isValidDirection(snake.getCurrentDirection(), dir)) {
				return Integer.MIN_VALUE;
			}

			if (gameState.getBoard().hasFruit(resultPosition)) {
				return 10;
			}

			if (gameState.getBoard().hasWall(resultPosition)) {
				return -100;
			}

			return -1;
		}catch(ArrayIndexOutOfBoundsException e){
		return Integer.MIN_VALUE;
		}
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

	class ScoredDirection {
		Direction direction;
		int score;

		ScoredDirection(Direction dir, int s){
			direction = dir;
			score = s;
		}
	}
}
