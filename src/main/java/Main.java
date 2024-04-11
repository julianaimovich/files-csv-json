import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String csvFileName = "src/main/resources/data.csv";
        String xmlFileName = "src/main/resources/data.xml";

        List<Employee> listFromCsv = parseCSV(columnMapping, csvFileName);
        String jsonFromCsv = listToJson(listFromCsv);
        writeString(jsonFromCsv, "data.json");

        List<Employee> listFromXml = parseXML(xmlFileName);
        String jsonFromXml = listToJson(listFromXml);
        writeString(jsonFromXml, "data2.json");
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> employeeList = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            employeeList = csv.parse();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return employeeList;
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileName));
            Node root = document.getDocumentElement();
            NodeList employeeNodeList = root.getChildNodes();

            for (int i = 0; i < employeeNodeList.getLength(); i++) {
                Node currentEmployee = employeeNodeList.item(i);

                if (currentEmployee.getNodeType() == Node.ELEMENT_NODE) {
                    Employee employee = new Employee();
                    NodeList attributes = currentEmployee.getChildNodes();

                    for (int a = 0; a < attributes.getLength(); a++) {
                        Node currentNode = attributes.item(a);

                        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) currentNode;
                            String attrName = element.getNodeName();
                            String attrValue = element.getTextContent();
                            Field field = Employee.class.getDeclaredField(attrName);

                            if (field.getType().equals(long.class)) {
                                field.set(employee, Long.parseLong(attrValue));
                            } else if (field.getType().equals(int.class)) {
                                field.set(employee, Integer.parseInt(attrValue));
                            } else {
                                field.set(employee, attrValue);
                            }
                        }
                    }
                    employeeList.add(employee);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employeeList;
    }

    public static <T> String listToJson(List<T> list) {
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(list, listType);
    }

    public static void writeString(String jsonString, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonString);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}