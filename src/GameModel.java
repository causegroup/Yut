import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class GameModel extends Observable {
    ArrayList<Player> players;
    Yut yut = new Yut();
    int turn;
    ArrayList<GamePiece> selectedPieces;
    GameBoard gameBoard = new GameBoard();

    public void createPlayer(int playerNum) {
        for(int i = 0; i < playerNum; i++) {
            players.add(new Player(i));
        }
    }

    public void createPiece (int pieceNum){
        for (Player player: this.players) {
            player.createPiece(pieceNum);
        }
    }

    public void start(){
        turn = 0;
        players.get(turn).startTurn();
    }

    /*윷 던지는 버튼이 클릭되었을 때*/
    public void yutClickEvent(){
        Player currentPlayer = players.get(turn);
        if(currentPlayer.phase == Phase.THROW_YUT_PHASE){
            int yutNum = yut.throwYut();
            currentPlayer.yutNums.add(yutNum);
            if(yutNum < 4){
                currentPlayer.phase = Phase.CHOOSE_PIECE_PHASE;
            }
        }
    }

    /*아직 보드에 올라가지 않은 게임말이 클릭되었을 때*/
    public void pieceOutsideBoardClickEvent(GamePiece gamePiece){
        Player currentPlayer = gamePiece.owner;
        if(currentPlayer.playerID == turn){
            if(currentPlayer.phase == Phase.CHOOSE_PIECE_PHASE) {
                selectedPieces = new ArrayList<GamePiece>();
                selectedPieces.add(gamePiece);
                currentPlayer.phase = Phase.MOVE_PIECE_PHASE;
            }
            /*선택을 취소하고 싶은 경우*/
            else if (currentPlayer.phase == Phase.MOVE_PIECE_PHASE && selectedPieces.get(0) == gamePiece){
                selectedPieces = null;
                currentPlayer.phase = Phase.CHOOSE_PIECE_PHASE;
            }
        }
    }


    /*게임판의 노드 1~29번째가 클릭되었을 때*/
    public void nodeClickEvent(Node node) {
        Player currentPlayer = players.get(turn);

        /*움직일 노드를 고르는 단계일 때*/
        if(currentPlayer.phase == Phase.CHOOSE_PIECE_PHASE){
            /*빈 노드를 선택한 경우*/
            if(node.getGamePiecesOn() == null){
                return;
            }
            /*자기 말이 있는 노드를 선택한 경우*/
            else if(node.getGamePiecesOn().get(0).owner == currentPlayer) {
                selectedPieces = node.getGamePiecesOn();
                currentPlayer.phase = Phase.MOVE_PIECE_PHASE;
            }
            else {
                /*do nothing*/
                return;
            }
        }

        /*말이 움직이는 단계일 때*/
        else if(currentPlayer.phase == Phase.MOVE_PIECE_PHASE){
            /*이동할 수 있는 칸인 경우*/
            if(selectedPieces.get(0).getMovableNodes(currentPlayer.yutNums).contains(node) == true){
                /*차이 어떻게 구하지*/
                currentPlayer.yutNums.remove((Integer));
                if(node.getGamePiecesOn() == null){
                    /*그냥 이동하는 경우*/
                    for(GamePiece piece : selectedPieces){
                        piece.move(node);
                    }
                }
                else if(node.getGamePiecesOn().get(0).owner == currentPlayer){
                    /*업는 경우*/
                    for(GamePiece piece : selectedPieces){
                        piece.move(node);
                    }
                }
                else if(node.getGamePiecesOn().get(0).owner != currentPlayer){
                    /*상대의 말을 잡은 경우*/
                    ArrayList<GamePiece> caughtPieces = node.getGamePiecesOn();
                    for(GamePiece caughtPiece : caughtPieces){
                        caughtPiece.caught();
                    }
                    for(GamePiece piece : selectedPieces) {
                        piece.move(node);
                    }
                }
                selectedPieces = null;
                if(currentPlayer.throwCnt == 0 && currentPlayer.yutNums.size() == 0){
                    changeTurn();
                }
            }
            /*이동할 수 없는 칸인 경우 또는 자기 자신을 한번 더 클릭한 경우*/
            else {
                currentPlayer.phase = Phase.CHOOSE_PIECE_PHASE;
                selectedPieces = null;
            }

        }
        setChanged();
        notifyObservers();
    }

    private void changeTurn(){
        players.get(turn).endTurn();
        turn = (turn + 1) % players.size();
        players.get(turn).startTurn();
    }

    void makeOneStep() {
        setChanged();
        notifyObservers();
    } // end of makeOneStep method
} // end of Model class

enum Phase {
    THROW_YUT_PHASE(0),
    CHOOSE_PIECE_PHASE(1),
    MOVE_PIECE_PHASE(2);

    private int phase;
    Phase(int phase){
        this.phase = phase;
    }

}

class Player{
    Phase phase;
    int playerID;
    ArrayList<Integer> yutNums;
    ArrayList<GamePiece> gamePieces;
    int throwCnt;

    Player(int playerID){
        this.playerID = playerID;
        throwCnt = 0;
    }
    public void createPiece(int cnt){
        for(int pieceID = 0; pieceID < cnt; pieceID++) {
            gamePieces.add(new GamePiece(this, pieceID));
        }
    }

    public GamePiece getGamePieceById(int pieceID) {
        return gamePieces.get(pieceID);
    }

    public void startTurn(){
        phase = Phase.THROW_YUT_PHASE;
        throwCnt = 1;
    }
    public void endTurn(){
        yutNums.clear();
    }

}

class GameBoard{
    public Node[] Node = new Node[29];
}

class Node{
    private ArrayList<GamePiece> gamePiecesOn;

    Node(){
        gamePiecesOn = null;
    }
    public void setGamePiecesOn(ArrayList<GamePiece> gamePieces) {
        this.gamePiecesOn = gamePieces;
    }

    public ArrayList<GamePiece> getGamePiecesOn() {
        return gamePiecesOn;
    }
}

enum Status {
    OFF_BOARD(0),
    ON_BOARD(1),
    FINISHED(2);

    private int status;
    Status(int status){
        this.status = status;
    }
}

class GamePiece{
    public Status status;
    public Player owner;
    public int pieceID;
    public Node node;
    GamePiece(Player owner, int pieceID){
        status = Status.OFF_BOARD;
        this.owner = owner;
        this.pieceID  = pieceID;
        node = null;
    }

    public ArrayList<Node> getMovableNodes(ArrayList<Integer> yutNums){
        return;
    }

    public void move(Node nextNode){
        node.getGamePiecesOn().remove(this);
        nextNode.getGamePiecesOn().add(this);
        node = nextNode;
    }

    public void caught(){
        node.getGamePiecesOn().remove(this);
        node = null;
        this.status = Status.OFF_BOARD;
    }
}

class Yut{
    private int value;
    Random generator = new Random();
    public int throwYut(){
        value = generator.nextInt(6) + 1;
        if(value == 6)
            value = -1;
        return value;
    }

    public int getValue() {
        return value;
    }
}