package mazeCreator;
/**
 * This class represents the Strategy interface. It 
 * makes the class Maze (=Context) independent on how the maze algorithms
 * are implemented.
 * 
 * @author AmazingGroup 
 * 
 */

public interface MazeCreator {
	public void setUpStructure(Cell[][] mazeFields);

	public void createMaze(Cell startingField, Cell endField);
}

