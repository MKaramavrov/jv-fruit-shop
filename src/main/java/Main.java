import dao.FruitsDao;
import dao.FruitsDaoImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.FruitTransaction;
import service.FruitHandler;
import service.impl.BalanceOfFruits;
import service.impl.PurchaseFruits;
import service.impl.ReturnFruits;
import service.impl.SupplyFruits;
import servicecsv.CreateReport;
import servicecsv.DataHandler;
import servicecsv.FileReaderService;
import servicecsv.FileWriterService;
import servicecsv.impl.CreateReportImpl;
import servicecsv.impl.DataHandlerImpl;
import servicecsv.impl.FileReaderImpl;
import servicecsv.impl.FileWriterImpl;
import strategy.OperationStrategy;
import strategy.impl.OperationStrategyImpl;

public class Main {
    private static final String OPERATION_PATH = "src/main/resources/operation.csv";
    private static final String REPORT_PATH = "src/main/resources/report.csv";

    public static void main(String[] args) {
        Map<String, FruitTransaction.Operation> transactionHandlerMap = new HashMap<>();
        transactionHandlerMap.put("b", FruitTransaction.Operation.BALANCE);
        transactionHandlerMap.put("s", FruitTransaction.Operation.SUPPLY);
        transactionHandlerMap.put("r", FruitTransaction.Operation.RETURN);
        transactionHandlerMap.put("p", FruitTransaction.Operation.PURCHASE);

        FruitsDao fruitsDao = new FruitsDaoImpl();
        Map<FruitTransaction.Operation, FruitHandler> fruitHandlerMap = new HashMap<>();
        fruitHandlerMap.put(FruitTransaction.Operation.BALANCE, new BalanceOfFruits(fruitsDao));
        fruitHandlerMap.put(FruitTransaction.Operation.SUPPLY, new SupplyFruits(fruitsDao));
        fruitHandlerMap.put(FruitTransaction.Operation.RETURN, new ReturnFruits(fruitsDao));
        fruitHandlerMap.put(FruitTransaction.Operation.PURCHASE, new PurchaseFruits(fruitsDao));

        FileReaderService fileReaderService = new FileReaderImpl();
        OperationStrategy operationStrategy = new OperationStrategyImpl(fruitHandlerMap);
        DataHandler dataHandler = new DataHandlerImpl(transactionHandlerMap);
        List<FruitTransaction> list =
                dataHandler.handleData(new FileReaderImpl().readTheFruitsStorage(OPERATION_PATH));
        for (FruitTransaction fruitTransaction : list) {
            operationStrategy.get(fruitTransaction.getOperation())
                    .handleOperation(fruitTransaction);
        }
        CreateReport reportCreator = new CreateReportImpl();
        dataHandler.handleData(fileReaderService.readTheFruitsStorage(OPERATION_PATH));

        List<String> listOfFruits = reportCreator.createReport(fruitsDao.getCurrentFruitAmount());
        FileWriterService fileWriterService = new FileWriterImpl();
        fileWriterService.writeToFile(REPORT_PATH, listOfFruits);
    }
}
