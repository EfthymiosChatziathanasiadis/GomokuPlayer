import java.awt.Color;

/*
* AUTHOR: EFTHYMIOS CHATZIATHANASIADIS
* STUDENT ID: 150359131
* ECS629 ARTIFICIAL INTELLIGENCE
* GUMOKU PROJECT
*/

public class Player150359131 extends GomokuPlayer{
  int MAX = 0;
  int MIN = 0;
/*
*PURPOSE: Class used to encapsulate information(heuristic value, move position) when the recursive minimax returns.
*/
  class Node{
    //move coordinates
   public int x;
   public int y;
   //heuristic Value
   public double value;

   public Node(double value, int x, int y){
     this.value = value;
     this.x = x;
     this.y = y;
   }

  }

  @Override
  public Move chooseMove(Color [][] board, Color me){
    int [][] convertBoard = convertBoard(board); //convert board from color to int to speedup search
    MAX = convertColor(me);//MAX player
    MIN = changeTurn(MAX);//MIN player
    Node rootState = minimax(convertBoard);
    return new Move(rootState.x, rootState.y);
  }

  public Node minimax(int [][] board){
    return max(board, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }
  //MIN player
  public Node min(int [][] state, int depth, double alpha, double beta){
    if(cutoffTest(state, depth))return heuristicValue(state);
    double score = Integer.MAX_VALUE;
    int x = 0;
    int y = 0;
    Node tempState = null;
    outerloop:
    for(int i = 0; i < 8; i++){//nested loop finds all possible moves
      for(int j = 0; j < 8; j++){
        if(state[i][j] == 0){//valid move
          state[i][j] = MIN;
          tempState = max(state, depth+1, alpha, beta);
          state[i][j] = 0;//revert
          if(tempState.value < score){
            score = tempState.value;
            x = i;
            y = j;
          }

          if(score <= alpha){
            //pruning
            tempState.value = score;
            tempState.x = x;
            tempState.y = y;
            return tempState;
          }

          beta = Math.min(beta, score);

        }

      }
    }
    tempState.value = score;
    tempState.x = x;
    tempState.y = y;
    return tempState;
  }
  //MAX Player
  public Node max(int [][] state, int depth, double alpha, double beta){
    if(cutoffTest(state, depth))return heuristicValue(state);
    double score = Integer.MIN_VALUE;
    int x = 0;
    int y = 0;
    Node tempState = null;
    outerloop:
    for(int i = 0; i < 8; i++){
      for(int j = 0; j < 8; j++){
        if(state[i][j] == 0){
          state[i][j] = MAX;
          tempState = min(state, depth+1, alpha, beta);
          state[i][j] = 0;//revert
          if(tempState.value > score){
            score = tempState.value;
            x = i;
            y = j;
          }

          if(score >= beta){
            //pruning
            tempState.value = score;
            tempState.x = x;
            tempState.y = y;
            return tempState;
          }

          alpha = Math.max(alpha, score);

        }
      }
    }

    tempState.value = score;
    tempState.x = x;
    tempState.y = y;
    return tempState;
  }
  /*
  *GOAL: Identify a terminal state
  *Input: state board, depth
  *Output: terminal or not
  *Conditions:
  * - depth limit reached
  * - there is a winner
  * - the board is filled fully
  */
  public boolean cutoffTest(int [][] state, int depth){
    boolean test = false;
    if( (depth >= 4) || (checkForWinner(state) != 0)  || allFilled(state)){
      test = true;
    }
    return test;
  }
  /*
  *GOAL: Evaluate the state of the board and give it a numerical score
  *Hign score indicate good state, Low score indicate bad state
  *Input: state board
  *Output: Score encapsulated in class node
  *EVAL(S):
  * EVAL(S) =
  * -large number if player wins
  * -large negative number if opponent wins
  * -Vattack - Vdefense otherwise
  */
  public Node heuristicValue(int [][] state){
    double heuristicEvaluation = 0;
    boolean filled = allFilled(state);
    int winner = checkForWinner(state);
    if(filled){//board is filled fully
      if(winner == 0)//draw i.e. no winner
        heuristicEvaluation = 50000;
      else if(winner == MAX)//winner is MAX
        heuristicEvaluation = 100000;
      else//winner is MIN
        heuristicEvaluation = -100000;

    }else{//board not filled

        if(winner == 0){//no winner
          int [] attackVector = sequenceCounter(state, MAX);//player attack vector
          int [] defenseVectore = sequenceCounter(state, MIN);//player defense vector
          /*
          *HEURISTIC EVALUATION: weighted sum(weights are set to prioritize victory) of the attack and defense vectors:
          *  Vattack + Vdefense =
          *  w1*(F1attack-F1defense)+
          *  w2*(F2attack-F2defense)+
          *  w3*(F3attack-F3defense)+
          *  w4*(F4attack-F4defense)+
          */

          heuristicEvaluation = 6 * (attackVector[3] - defenseVectore[3] ) + 3 * (attackVector[2] - defenseVectore[2]) +  (attackVector[1] - defenseVectore[1]) + (attackVector[0] - defenseVectore[0]);
          /*
          *TESTING Heuristic with different weights
          * heuristicEvaluation = 6 * (attackVector[3] - defenseVectore[3] ) + 3 * (attackVector[2] - (defenseVectore[2])) +  (attackVector[1] - defenseVectore[1]) + (attackVector[0] - defenseVectore[0]);
          * heuristicEvaluation = 9 * (attackVector[3] - defenseVectore[3] ) + 5 * (attackVector[2] - defenseVectore[2]) +  2 * (attackVector[1] - defenseVectore[1]) + (attackVector[0] - defenseVectore[0]);
          */
        }else if(winner == MAX)//max winner
          heuristicEvaluation = 100000;
         else//MIN winner
          heuristicEvaluation = -100000;

    }

    return new Node(heuristicEvaluation, 0, 0);//heuristic value encapsulated in object of type Node

  }
  /*
  *GOAL: Check if there is a winner
  *Input: board state
  *Output: winner i.e. black or white
  */
  public int checkForWinner(int [][] state){
    int winner = winner(state);
    if(winner == MAX)return MAX;
    else if(winner == MIN)return MIN;
    else return 0;
  }

  /*
  *GOAL: To count all possible winning sequences
  *WINNING SEQUENCE:
  * -contains consequtive 5 blocks of player cells or null cells. contains NO opponent pieces
  * -can be vertical, horizontal, diagonal
  *
  *Input: Board state and the player
  *
  *Output: Vector V[F1,F2,F3,F4,F5]
  *
  *F1: # of winning sequences containing 1 player cell
  *F2: # of winning sequences containing 2 player cells
  *F3: # of winning sequences containing 3 player cells
  *F4: # of winning sequences containing 4 player cells
  *F5: # of winning sequences containing 5 player cells
  */
  public int [] sequenceCounter(int [][] board, int player){

    int onePieceSequenceCounter = 0;
    int twoPieceSequenceCounter = 0;
    int threePieceSequenceCounter = 0;
    int fourPieceSequenceCounter = 0;
    int fivePieceSequenceCounter = 0;


    int[][] directions = {{1,0}, {1,-1}, {1,1}, {0,1}};//all directions i.e. vertical, horizontal, diagonal
    for (int[] d : directions) {//check each position of the board for all directions
        int dx = d[0];
        int dy = d[1];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int lastx = x + 4*dx;//5th x cordinate of the 5-sequence starting from x position
                int lasty = y + 4*dy;//5th y cordinate of the 5-sequence starting from y position
                if (0 <= lastx && lastx < 8 && 0 <= lasty && lasty < 8) {//check if there is a five-sequence from x,y position
                  //sequence
                  int [] sequence = {
                     board[x][y],
                     board[x+dx][y+dy],
                     board[x+2*dx][y+2*dy],
                     board[x+3*dx][y+3*dy],
                     board[lastx][lasty]
                   };
                   //check sequence to see if valid
                   if(validSequence(sequence, player)){
                     //count player pieces in the valid sequence and increase the according feature of the vector
                     int count = countPlayerPieces(sequence, player);
                     if(count == 1)onePieceSequenceCounter++;
                     else if(count == 2)twoPieceSequenceCounter++;
                     else if(count == 3)threePieceSequenceCounter++;
                     else if(count == 4)fourPieceSequenceCounter++;
                     else if(count == 5)fivePieceSequenceCounter++;

                   }
                }
            }
        }
    }

    //player feature vector
    int [] sequenceCounters = {onePieceSequenceCounter,twoPieceSequenceCounter, threePieceSequenceCounter, fourPieceSequenceCounter, fivePieceSequenceCounter };
    return sequenceCounters;
  }

  /*
  *GOAL: Check if there is a winner
  *Input: board state
  *Output: winner i.e. black or white
  */
  public int winner(int [][] board){

    int[][] directions = {{1,0}, {1,-1}, {1,1}, {0,1}};
    int winner = 0;
    outerloop:
    for (int[] d : directions) {
        int dx = d[0];
        int dy = d[1];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int lastx = x + 4*dx;
                int lasty = y + 4*dy;
                if (0 <= lastx && lastx < 8 && 0 <= lasty && lasty < 8) {
                  //valid
                  if(board[x][y] != 0 &&
                     board[x][y] ==  board[x+dx][y+dy] &&
                     board[x][y] == board[x+2*dx][y+2*dy] &&
                     board[x][y] == board[x+3*dx][y+3*dy] &&
                     board[x][y] == board[lastx][lasty]){
                       winner = board[x][y];
                       break outerloop;
                     }

                }
            }
        }
    }
   return winner;
  }


/*
*GOAL: count the number of player pieces of a valid winning sequence
*Input: winning sequence, player
*Output: number of player pieces in the sequence i.e. 0 or 1 or 2 or 3 or 4 or 5
*/
 int countPlayerPieces(int [] sequence, int player){
   int count = 0;
   for(int i = 0; i < 5; i++ ){
     if(sequence[i] == player){
       count++;
     }
   }
   return count;
 }
 /*
 *Goal: Check validity of sequence i.e. winning or not
 *Input: sequence, player
 *Output: valid or not valid
 */
 boolean validSequence(int [] sequence, int player){
   boolean valid = true;
   for(int i = 0; i < 5; i++){
     if(sequence[i] == changeTurn(player)){
       valid = false;
       break;
     }
   }
   return valid;
 }

  /*
  *Goal: Check if the board has been filled fully
  *Input: board state
  *Output: filled or not filled
  *
  */
  public boolean allFilled(int [][] state){
    boolean filled = true;
    outerloop:
    for(int i = 0; i < 8; i++){
      for(int j = 0; j < 8; j++){
        if(state[i][j] == 0){
          filled = false;
          break outerloop;
        }
      }
    }
    return filled;
  }

  private int changeTurn(int player){
    if(player == 1)return -1;
    else  return 1;
  }
  //Convert board from Color to int to speedup search
  public int [][] convertBoard(Color [][] board){
    int [][] convertedBoard = new int [8][8];
    for(int i = 0; i < 8; i++){
      for(int j = 0; j < 8; j++){
          if(board[i][j] == Color.black)convertedBoard[i][j] = 1;
          else if(board[i][j] == Color.white)convertedBoard[i][j] = -1;
          else convertedBoard[i][j] = 0;
      }
    }
    return convertedBoard;
  }
  public int convertColor(Color player){
    if(player == Color.white)return -1;
    else return 1;
  }
  /*
  *DEBUGGING methods
  */
  public void printBoard(int [][] board){
    for(int i = 0; i < 8; i++){
      for(int j = 0; j < 8; j++){
        System.out.print(getColor(board[i][j])+" ");
      }
      System.out.println();
    }

  }
  public String getColor(int p){
    if(p == 1)return "B";
    else if(p == -1)return "W";
    else return "*";
  }




}
