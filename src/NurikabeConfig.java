import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * A class to represent a single configuration in the Nurikabe puzzle.
 *
 * @author Sean Strout @ RITCS
 * @author Kyle McCoy
 */
public class NurikabeConfig implements Configuration {

    // TODO
    private char[][] board ;
    private int last_move_col ;
    private int last_move_row ;
    private int columns ;
    private int rows ;

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3 3          # rows columns<br>
     * 1 . #        # row 1, .=empty, 1-9=numbered island, #=island, &#64;=sea<br>
     * &#64; . 3    # row 2<br>
     * 1 . .        # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws FileNotFoundException if the file is not found
     */
    public NurikabeConfig(String filename) throws FileNotFoundException {
        try (Scanner in = new Scanner(new File(filename))) {
            // TODO
            String line = in.nextLine() ;
            String[] row_column = line.split(" ") ;
            int row = Integer.parseInt(row_column[0]) ;
            int column = Integer.parseInt(row_column[1]) ;
            columns = column ;
            rows = row ;
            board = new char[row][column] ;
            for(int i = 0; i < row; i++){
                String next_line = in.nextLine() ;
                String[] tiles = next_line.split(" ") ;
                for(int j = 0; j < column; j++){
                    if(tiles[j].equals("&#64;")){
                        board[i][j] = '@' ;
                    }
                    else{
                        board[i][j] = tiles[j].charAt(0) ;
                    }
                }
            }
            last_move_col = -1 ;
            last_move_row = 0 ;
        }

    }

    /**
     * The copy constructor takes a config, other, and makes a full "deep" copy
     * of its instance data.
     *
     * @param other the config to copy
     */
    protected NurikabeConfig(NurikabeConfig other, boolean isLand) {
        // TODO
        this.last_move_row = other.last_move_row ;
        this.last_move_col = other.last_move_col ;
        this.rows = other.rows ;
        this.columns = other.columns ;
        this.board = new char[other.rows][other.columns] ;
        this.last_move_col++ ;
        if(this.last_move_col >= this.columns){
            this.last_move_col = 0 ;
            this.last_move_row++ ;
        }
        for(int i = 0; i < this.rows; i++){
            System.arraycopy(other.board[i], 0, this.board[i], 0, this.columns);
        }
        while(this.board[this.last_move_row][this.last_move_col]!='.'){
            this.last_move_col++ ;
            if(this.last_move_col >= this.columns){
                this.last_move_col = 0 ;
                this.last_move_row++ ;
            }
        }
        if(isLand){
            this.board[this.last_move_row][this.last_move_col] = '#' ;
        }else{
            this.board[this.last_move_row][this.last_move_col] = '@' ;
        }
    }

    @Override
    public Collection<Configuration> getSuccessors() {
        // TODO
        List<Configuration> successors = new LinkedList<>() ;
        NurikabeConfig copy_land = new NurikabeConfig(this, true) ;
        successors.add(copy_land);
        NurikabeConfig copy_sea = new NurikabeConfig(this, false) ;
        successors.add(copy_sea);
        return successors ;
    }

    public boolean noPools(){
        if (this.board[this.last_move_row][this.last_move_col] != '@') {
            return true ;
        }
        boolean checkN = false ;
        boolean checkNW = true ;
        boolean checkW = false ;
        if( (this.last_move_col-1>= 0)&&(this.board[this.last_move_row][this.last_move_col-1]=='@') ){
            checkW = true ;
        }
        if( (this.last_move_row-1>=0)&&(this.board[this.last_move_row-1][this.last_move_col]=='@') ){
            checkN = true ;
        }
        if (checkN && checkW){
            if(this.board[this.last_move_row-1][this.last_move_col-1]=='@'){
                checkNW = false ;
            }
        }
        return checkNW ;
    }

    public boolean allSeaConnects(){
        int total_number_sea_cells = 0 ;
        int start_row = 0;
        int start_col = 0;
        boolean first_sea_cell = true ;
        for( int i = 0; i < this.rows; i++){
            for( int j = 0; j < this.columns; j++){
                if(this.board[i][j]=='@'){
                    total_number_sea_cells++ ;
                    if(first_sea_cell){
                        first_sea_cell = false ;
                        start_col = j ;
                        start_row = i ;
                    }
                }
            }
        }
        Set<ArrayList<Integer>> visited = new HashSet<>() ;
        ArrayList<Integer> start_cell = new ArrayList<>() ;
        start_cell.add(start_row) ;
        start_cell.add(start_col) ;
        visited.add(start_cell) ;
        int dfs_search = 1 + countCellsDFS(start_cell, visited, '@') ;
        System.out.println(dfs_search) ;
        System.out.println(total_number_sea_cells);
        return dfs_search == total_number_sea_cells ;
    }

    public int countCellsDFS(ArrayList<Integer> start, Set<ArrayList<Integer>> visited, char symbol){
        int count = 0 ;
        int row = start.get(0) ;
        int col = start.get(1) ;

        // check N
        ArrayList<Integer> north = new ArrayList<>() ;
        north.add(row-1) ;
        north.add(col) ;
        if(row-1 >= 0 && this.board[row-1][col] == symbol && !visited.contains(north)){
            visited.add(north) ;
            count = count + 1 + countCellsDFS(north, visited, symbol) ;
        }

        // check S
        ArrayList<Integer> south = new ArrayList<>() ;
        south.add(row+1) ;
        south.add(col) ;
        if(row+1 < this.rows && this.board[row+1][col] == symbol && !visited.contains(south)){
            visited.add(south) ;
            count = count + 1 + countCellsDFS(south, visited, symbol) ;
        }

        // check E
        ArrayList<Integer> east = new ArrayList<>() ;
        east.add(row) ;
        east.add(col+1) ;
        if(col+1 < this.columns && this.board[row][col+1] == symbol && !visited.contains(east)){
            visited.add(east) ;
            count = count + 1 + countCellsDFS(east, visited, symbol) ;
        }

        // check S
        ArrayList<Integer> west = new ArrayList<>() ;
        west.add(row) ;
        west.add(col-1) ;
        if(col-1 >= 0 && this.board[row][col-1] == symbol && !visited.contains(west)){
            visited.add(north) ;
            count = count + 1 + countCellsDFS(west, visited, symbol) ;
        }

        return count ;
    }

    public boolean landTouch(ArrayList<Integer> start, Set<ArrayList<Integer>> visited){
        boolean touch = false ;
        int row = start.get(0) ;
        int col = start.get(1) ;
        ArrayList<Character> parts = new ArrayList<>() ;
        parts.add('#');
        for(int i = 0; i < 10; i++){
            parts.add((char)i) ;
        }

        // check N
        ArrayList<Integer> north = new ArrayList<>() ;
        north.add(row-1) ;
        north.add(col) ;
        if(row-1 >= 0 && parts.contains(this.board[row-1][col]) && !visited.contains(north)){
            if(this.board[row-1][col]!='#'){
                touch = true ;
            }else{
                visited.add(north) ;
                touch = landTouch(north, visited) ;
            }
        }

        // check S
        ArrayList<Integer> south = new ArrayList<>() ;
        south.add(row+1) ;
        south.add(col) ;
        if(row+1 < this.rows && parts.contains(this.board[row+1][col]) && !visited.contains(south)){
            if(this.board[row+1][col]!='#'){
                touch = true ;
            }else{
                visited.add(south) ;
                touch = landTouch(south, visited) ;
            }
        }

        // check W
        ArrayList<Integer> west = new ArrayList<>() ;
        west.add(row) ;
        west.add(col-1) ;
        if(col-1 >= 0 && parts.contains(this.board[row][col-1]) && !visited.contains(west)){
            if(this.board[row][col-1]!='#'){
                touch = true ;
            }else{
                visited.add(west) ;
                touch = landTouch(west, visited) ;
            }
        }

        // check E
        ArrayList<Integer> east = new ArrayList<>() ;
        east.add(row) ;
        east.add(col+1) ;
        if(col+1 < this.columns && parts.contains(this.board[row][col+1]) && !visited.contains(east)){
            if(this.board[row][col+1]!='#'){
                touch = true ;
            }else{
                visited.add(east) ;
                touch = landTouch(east, visited) ;
            }
        }

        return touch ;
    }

    public boolean noLandConnects(){
        ArrayList<Integer> numbered_land = new ArrayList<>() ;
        ArrayList<ArrayList<Integer>> coordinates = new ArrayList<>() ;
        for( int i = 0; i < this.rows; i++){
            for( int j = 0; j < this.columns; j++){
                if( board[i][j] != '@' && board[i][j] != '#' ){
                    numbered_land.add(Integer.parseInt(board[i][j] + "")) ;
                    ArrayList<Integer> coord = new ArrayList<>() ;
                    coord.add(i) ;
                    coord.add(j) ;
                    coordinates.add(coord) ;
                }
            }
        }
        for(int index = 0; index < numbered_land.size(); index++){
            Set<ArrayList<Integer>> visited = new HashSet<>() ;
            ArrayList<Integer> start_cell = coordinates.get(index) ;
            visited.add(start_cell) ;
            boolean dfs_search = landTouch(start_cell, visited) ;
            if (dfs_search){
                return false ;
            }
        }
        return true ;
    }

    public boolean IslandNumberCountCheck(){
        ArrayList<Integer> numbered_land = new ArrayList<>() ;
        ArrayList<ArrayList<Integer>> coordinates = new ArrayList<>() ;
        for( int i = 0; i < this.rows; i++){
            for( int j = 0; j < this.columns; j++){
                if( board[i][j] != '@' && board[i][j] != '#' && board[i][j] != '.'){
                    numbered_land.add(Integer.parseInt(board[i][j] + "")) ;
                    ArrayList<Integer> coord = new ArrayList<>() ;
                    coord.add(i) ;
                    coord.add(j) ;
                    coordinates.add(coord) ;
                }
            }
        }
        for(int index = 0; index < numbered_land.size(); index++){
            Set<ArrayList<Integer>> visited = new HashSet<>() ;
            ArrayList<Integer> start_cell = coordinates.get(index) ;
            visited.add(start_cell) ;
            int dfs_search = 1 + countCellsDFS(start_cell, visited, '#') ;
            if( dfs_search > numbered_land.get(index)){
                return false ;
            }
        }
        return true ;
    }

    @Override
    public boolean isValid() {
        // TODO
        if(!noPools()){
           return false ;
        }
        if(!IslandNumberCountCheck()){
            return false ;
        }
        if(isGoal()){
            System.out.println(allSeaConnects());
            System.out.println(noLandConnects());
            return allSeaConnects() && noLandConnects() ;
        }
        return true ;
    }

    @Override
    public boolean isGoal() {
        // TODO
        return (this.last_move_col == this.columns - 1) && (this.last_move_row == this.rows - 1);
    }

    /**
     * Returns a string representation of the puzzle, e.g.: <br>
     * <tt><br>
     * 1 . #<br>
     * &#64; . 3<br>
     * 1 . .<br>
     * </tt><br>
     */
    @Override
    public String toString() {
        // TODO
        StringBuilder result = new StringBuilder();
        for (int row=0; row<this.rows; ++row) {
            result.append("\n");
            for (int col=0; col<this.columns; ++col) {
                result.append(this.board[row][col]);
                result.append(" ");
            }
        }
        return result.toString();
    }
}
