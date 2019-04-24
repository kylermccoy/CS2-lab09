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
    // 2D array to keep track of what lies where
    private char[][] board ;
    // last column where a move was made
    private int last_move_col ;
    // last row where a move was made
    private int last_move_row ;
    // total columns in game
    private int columns ;
    // total rows in game
    private int rows ;
    // max amount of land in game
    private int max_land ;
    // max amount of sea in game
    private int max_sea ;

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
            max_land = 0 ;
            max_sea = 0 ;
            columns = column ;
            rows = row ;
            ArrayList<Character> parts = new ArrayList<>() ;
            for(int i = 1; i < 10; i++){
                String piece = i + "";
                parts.add(piece.charAt(0)) ;
            }
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
                    if(parts.contains(tiles[j].charAt(0))){
                        max_land += (Integer.parseInt(tiles[j])) ;
                    }
                }
            }
            max_sea = (columns * rows) - max_land ;
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
        this.max_land = other.max_land ;
        this.max_sea = other.max_sea ;
        this.board = new char[other.rows][other.columns] ;
        this.last_move_col++ ;
        if(this.last_move_col >= this.columns){
            this.last_move_col = 0 ;
            this.last_move_row++ ;
        }
        for(int i = 0; i < this.rows; i++){
            System.arraycopy(other.board[i], 0, this.board[i], 0, this.columns);
        }
        while(this.last_move_row <= this.rows && this.board[this.last_move_row][this.last_move_col]!='.'){
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

    /**
     * Gets the successors of the current move
     * @return linkedlist of two copies of the game, one with the next move land,
     * and one with next move sea
     */
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

    /**
     * checks if an sea cells are formed in a 2x2 form
     * @return boolean if a pool has formed
     */
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

    /**
     * checks if all the sea cells are connected by checking if the dfs returns a count
     * matching the total amount of sea cells
     * @return boolean if all sea cells connect
     */
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
        return dfs_search == total_number_sea_cells ;
    }

    /**
     * DFS search to count the number of times the symbol occurs in the connected path
     * @param start arraylist containing coordinates for first node
     * @param visited set of already visited coordinates
     * @param symbol symbol to follow path
     * @return count of that symbol in dfs path
     */
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

        // check W
        ArrayList<Integer> west = new ArrayList<>() ;
        west.add(row) ;
        west.add(col-1) ;
        if(col-1 >= 0 && this.board[row][col-1] == symbol && !visited.contains(west)){
            visited.add(west) ;
            count = count + 1 + countCellsDFS(west, visited, symbol) ;
        }

        return count ;
    }

    /**
     * dfs checks if the numbered island is connected to another numbered island
     * @param start coordinates of starting numbered island
     * @param visited coordinates to be skipped
     * @return boolean true if numbered islands connects to another
     */
    public boolean landTouch(ArrayList<Integer> start, Set<ArrayList<Integer>> visited){
        boolean touch = false ;
        int row = start.get(0) ;
        int col = start.get(1) ;
        ArrayList<Character> parts = new ArrayList<>() ;
        parts.add('#');
        for(int i = 0; i < 10; i++){
            String piece = i + "" ;
            parts.add(piece.charAt(0)) ;
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

    /**
     * checks if the land is connected to more land
     * @return boolean if land doesnt connect
     */
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

    /**
     * counts that the numbered island has the number of land connected to it
     * @return boolean if count matches island number
     */
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
            if( dfs_search > numbered_land.get(index) || dfs_search < numbered_land.get(index)){
                return false ;
            }
        }
        return true ;
    }

    /**
     * checks to make sure land on board doesn't exceed max
     * @return boolean
     */
    public boolean landCountCheck(){
        int count = 0 ;
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.columns; j++){
                if(this.board[i][j]!='@' && this.board[i][j]!='.'){
                    count++ ;
                }
            }
        }
        return count <= max_land ;
    }

    /**
     * checks to make sure sea cells on board doesn't exceed max
     * @return boolean
     */
    public boolean seaCountCheck(){
        int count = 0 ;
        for(int i = 0; i < this.rows; i++){
            for(int j = 0; j < this.columns; j++){
                if(this.board[i][j]=='@'){
                    count++ ;
                }
            }
        }
        return count <= max_sea ;
    }

    /**
     * checks to ensure no numbered island doesn't go over the max amount of land
     * @return boolean
     */
    public boolean IslandNumberCountOverCheck(){
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
                return true ;
            }
        }
        return false ;
    }

    /**
     * checks to see if config is valid solution
     * @return boolean
     */
    @Override
    public boolean isValid() {
        // TODO
        if (!seaCountCheck()) {
            return false ;
        }
        if(!landCountCheck()){
            return false ;
        }
        if(!noPools()){
           return false ;
        }
        if(IslandNumberCountOverCheck()){
            return false ;
        }
        if(isGoal()){
            return allSeaConnects() && noLandConnects() && IslandNumberCountCheck() ;
        }
        return true ;
    }

    /**
     * checks to see if the board is full
     * @return boolean
     */
    @Override
    public boolean isGoal() {
        // TODO
        for( int i = 0; i < this.rows; i++){
            for( int j = 0; j < this.columns; j++){
                if(board[i][j] == '.'){
                    return false ;
                }
            }
        }
        return true ;
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
