import java.util.Scanner;

public class GameController {
    GameModel gameModel = new GameModel();
    Scanner scan = new Scanner(System.in);

    public void start(){

        gameModel.init(2,4);
        gameModel.start();

        while(true){
            System.out.println("========================================");
            System.out.println("player " + (gameModel.getTurn() + 1) + " 's turn");
            System.out.println("----------------------------------------");
            System.out.printf("%d\t %d\t %d\t %d\t %d\t %d\t \n",nc(10),nc(9),nc(8),nc(7),nc(6),nc(5));
            System.out.printf("%d\t %d\t  \t  \t %d\t %d\t \n",nc(11),nc(23),nc(21),nc(4));
            System.out.printf("%d\t  \t %d\t %d\t  \t %d\t \n",nc(12),nc(24),nc(22),nc(3));
            System.out.printf(" \t  \t   %d\t  \t  \t \n",nc(25));
            System.out.printf("%d\t  \t %d\t %d\t  \t %d\t \n",nc(13),nc(26),nc(28),nc(2));
            System.out.printf("%d\t %d\t  \t  \t %d\t %d\t \n",nc(14),nc(27),nc(29),nc(1));
            System.out.printf("%d\t %d\t %d\t %d\t %d\t %d\t \n",nc(15),nc(16),nc(17),nc(18),nc(19),nc(20));
            if(gameModel.getYutNums().size() > 0) {
                System.out.println("------------------------------------");
                System.out.println("윷 숫자 : " + gameModel.getYutNums().toString());
            }

            System.out.println("----------------------------------------");
            System.out.println("현재 페이즈: " + gameModel.getPhase());
            System.out.println("----------------------------------------");
            for(Node node : gameModel.getMovableNodes()){
                System.out.print(node.nodeID + " ");
            }
            System.out.println();
            System.out.println("----------------------------------------");
            int input = scan.nextInt();
            System.out.println("========================================");


            /*
            * 테스트를 위한 입력부분입니다 실제 구현할 때 숫자는 아무 신경 안쓰셔도됩니다.
            * 51~54: 게임판에 올라가지 않은 플레이어1의 말 선택
            * 61~64: 게임판에 올라가지 않은 플레이어2의 말 선택
            * 69: 백도, 71~75: 도 개 걸 윷 모
            * 77: 랜덤 윷 던지기
            * 버튼에 바인딩하시면 됨
            * */
            switch (input){
                case 51: case 52: case 53: case 54:
                    gameModel.pieceOutsideBoardClickEvent(gameModel.players.get(0).gamePieces.get(input-50));
                    break;
                case 61: case 62: case 63: case 64:
                    gameModel.pieceOutsideBoardClickEvent(gameModel.players.get(1).gamePieces.get(input-60));

                    break;
                case 77:
                    gameModel.randomYutClickEvent();
                    break;
                case 69: case 71: case 72: case 73: case 74: case 75:
                    gameModel.selectYutClickEvent(input-70);
                    break;
                default:
                    if(input > 29 || input < 1) {
                        System.out.println("error");
                        return;
                    }
                    gameModel.nodeClickEvent(gameModel.gameBoard.nodes[input]);

            }
        }
    }

    private int nc(int nodeIndex){
        if(gameModel.gameBoard.nodes[nodeIndex] == null)
            return 0;
        if(gameModel.gameBoard.nodes[nodeIndex].getGamePiecesOn().size() < 1)
            return 0;
        for(int i = 0; i < gameModel.players.size(); i++)
            if(gameModel.gameBoard.nodes[nodeIndex].getGamePiecesOn().get(0).owner.equals(gameModel.players.get(i)))
                return i + 1;
        return  0;
    }
}


