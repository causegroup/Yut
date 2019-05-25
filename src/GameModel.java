import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class GameModel extends Observable {
    ArrayList<Player> players = new ArrayList<Player>();
    Yut yut = new Yut();
    int turn;
    ArrayList<GamePiece> selectedPieces = new ArrayList<GamePiece>();
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

    public void init(int playerNum, int pieceNum){
        createPlayer(playerNum);
        createPiece(pieceNum);
        turn = 0;
    }
    public void start(){

        players.get(turn).startTurn();
    }

    /*윷 던지는 버튼이 클릭되었을 때*/
    public void randomYutClickEvent(){
        Player currentPlayer = players.get(turn);
        if(currentPlayer.phase == Phase.THROW_YUT_PHASE){
            currentPlayer.throwCnt--;
            int yutNum = yut.throwYut();
            currentPlayer.yutNums.add(yutNum);
            if(yutNum < 4){
                currentPlayer.phase = Phase.CHOOSE_PIECE_PHASE;
            }

        }
    }

    public void selectYutClickEvent(int select){
        Player currentPlayer = players.get(turn);
        if(currentPlayer.phase == Phase.THROW_YUT_PHASE){
            currentPlayer.throwCnt--;
            int yutNum = select;
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

        /*움직일 말을 고르는 단계일 때*/
        if(currentPlayer.phase == Phase.CHOOSE_PIECE_PHASE){
            /*빈 노드를 선택한 경우*/
            if(node.getGamePiecesOn() == null || node.getGamePiecesOn().size() < 1){
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
            if(gameBoard.getMovableNode(selectedPieces.get(0).getNode(),currentPlayer.yutNums.get(0)) == node){
                /*차이 어떻게 구하지*/
                currentPlayer.yutNums.remove(0);
                if(node.getGamePiecesOn() == null || node.getGamePiecesOn().size() < 1){
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
    ArrayList<Integer> yutNums = new ArrayList<Integer>();
    ArrayList<GamePiece> gamePieces = new ArrayList<GamePiece>();
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
    public Node[] nodes = new Node[31];
    final private Node[][] movableNodeTable = new Node[31][];

    public Node getMovableNode(Node currentNode, int yutNum){
        if(yutNum < 0)
            yutNum = 0;
        if(currentNode == null || currentNode == nodes[0])
            return movableNodeTable[0][yutNum];
        System.out.println(currentNode.nodeID);
        return movableNodeTable[currentNode.nodeID][yutNum];
    }

    GameBoard(){
        for(int i = 0; i < nodes.length; i++){
            nodes[i] = new Node(i);
        }
        /*
        게임판 위에 있는 것은 1번부터 29번째 노드
        보드에 아직 안올라간상태:0
        완주한 상태: 30
        */
        /*0일때 백도의 경우 예외처리 해줘야함*/
        movableNodeTable[0] = new Node[]{nodes[0],nodes[1],nodes[2],nodes[3],nodes[4],nodes[5]};

        movableNodeTable[1] = new Node[]{nodes[20],nodes[2],nodes[3],nodes[4],nodes[5],nodes[6]};
        movableNodeTable[2] = new Node[]{nodes[1],nodes[3],nodes[4],nodes[5],nodes[6],nodes[7]};
        movableNodeTable[3] = new Node[]{nodes[2],nodes[4],nodes[5],nodes[6],nodes[7],nodes[8]};
        movableNodeTable[4] = new Node[]{nodes[3],nodes[5],nodes[6],nodes[7],nodes[8],nodes[9]};
        movableNodeTable[5] = new Node[]{nodes[4],nodes[21],nodes[22],nodes[25],nodes[26],nodes[27]};
        movableNodeTable[6] = new Node[]{nodes[5],nodes[7],nodes[8],nodes[9],nodes[10],nodes[11]};
        movableNodeTable[7] = new Node[]{nodes[6],nodes[8],nodes[9],nodes[10],nodes[11],nodes[12]};
        movableNodeTable[8] = new Node[]{nodes[7],nodes[9],nodes[10],nodes[11],nodes[12],nodes[13]};
        movableNodeTable[9] = new Node[]{nodes[8],nodes[10],nodes[11],nodes[12],nodes[13],nodes[14]};
        movableNodeTable[10] = new Node[]{nodes[9],nodes[23],nodes[24],nodes[25],nodes[28],nodes[29]};
        movableNodeTable[11] = new Node[]{nodes[10],nodes[12],nodes[13],nodes[14],nodes[15],nodes[16]};
        movableNodeTable[12] = new Node[]{nodes[11],nodes[13],nodes[14],nodes[15],nodes[16],nodes[17]};
        movableNodeTable[13] = new Node[]{nodes[12],nodes[14],nodes[15],nodes[16],nodes[17],nodes[18]};
        movableNodeTable[14] = new Node[]{nodes[13],nodes[15],nodes[16],nodes[17],nodes[18],nodes[19]};
        movableNodeTable[15] = new Node[]{nodes[14],nodes[16],nodes[17],nodes[18],nodes[19],nodes[20]};
        movableNodeTable[16] = new Node[]{nodes[15],nodes[17],nodes[18],nodes[19],nodes[20],nodes[30]};
        movableNodeTable[17] = new Node[]{nodes[16],nodes[18],nodes[19],nodes[20],nodes[30],nodes[30]};
        movableNodeTable[18] = new Node[]{nodes[17],nodes[19],nodes[20],nodes[30],nodes[30],nodes[30]};
        movableNodeTable[19] = new Node[]{nodes[18],nodes[20],nodes[30],nodes[30],nodes[30],nodes[30]};
        movableNodeTable[20] = new Node[]{nodes[19],nodes[30],nodes[30],nodes[30],nodes[30],nodes[30]};

        movableNodeTable[21] = new Node[]{nodes[5],nodes[22],nodes[25],nodes[26],nodes[27],nodes[15]};
        movableNodeTable[22] = new Node[]{nodes[21],nodes[25],nodes[26],nodes[27],nodes[15],nodes[16]};
        movableNodeTable[23] = new Node[]{nodes[10],nodes[24],nodes[25],nodes[28],nodes[29],nodes[20]};
        movableNodeTable[24] = new Node[]{nodes[23],nodes[25],nodes[28],nodes[29],nodes[20],nodes[30]};
        movableNodeTable[25] = new Node[]{nodes[22],nodes[28],nodes[29],nodes[20],nodes[30],nodes[30]};
        movableNodeTable[26] = new Node[]{nodes[25],nodes[27],nodes[15],nodes[16],nodes[17],nodes[18]};
        movableNodeTable[27] = new Node[]{nodes[26],nodes[15],nodes[16],nodes[17],nodes[18],nodes[19]};
        movableNodeTable[28] = new Node[]{nodes[25],nodes[29],nodes[20],nodes[30],nodes[30],nodes[30]};
        movableNodeTable[29] = new Node[]{nodes[28],nodes[20],nodes[30],nodes[30],nodes[30],nodes[30]};
    }
}

class Node{
    public int nodeID;
    public ArrayList<GamePiece> gamePiecesOn;
    Node(int nodeID){
        this.nodeID = nodeID;
        gamePiecesOn = new ArrayList<GamePiece>();
    }

    public ArrayList<GamePiece> getGamePiecesOn() {
        return gamePiecesOn;
    }
}

class GamePiece{
    public Player owner;
    public int pieceID;
    private Node node;
    GamePiece(Player owner, int pieceID){
        this.owner = owner;
        this.pieceID  = pieceID;
        node = null;
    }

    public Node getNode() {
        return node;
    }

    public void move(Node nextNode){
        nextNode.getGamePiecesOn().add(this);
        if(node == null) {
            node = nextNode;
            return;
        }
        node.getGamePiecesOn().remove(this);
        node = nextNode;
    }

    public void caught(){
        if(node == null)
            return;
        node.getGamePiecesOn().remove(this);
        this.node = null;
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