package org.teachfx.antlr4.ep6;

import org.teachfx.antlr4.ep6.CSVParser.FieldContext;
import org.teachfx.antlr4.ep6.CSVParser.HeaderContext;
import org.teachfx.antlr4.ep6.CSVParser.RowContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVReader extends CSVBaseListener {
    List<String> tableHeader;
    List<Map<String, String>> tableList;
    Boolean isEnterHeader = false;

    public CSVReader() {
        this.tableHeader = new ArrayList<>();
        this.tableList = new ArrayList<>();
    }

    @Override
    public void enterHeader(HeaderContext ctx) {
        isEnterHeader = true;
        for (FieldContext item : ctx.row().field()) {
            tableHeader.add(item.getText());
        }
    }

    public void exitHeader(HeaderContext ctx) {
        isEnterHeader = false;

    }

    @Override
    public void enterRow(RowContext ctx) {
        if (!isEnterHeader) {
            Map<String, String> items = new HashMap<>();
            for (int i = 0; i < tableHeader.size(); ++i) {
                items.put(tableHeader.get(i), ctx.field(i).getText());
            }
            this.tableList.add(items);
        }
    }

    public void printTables() {
        System.out.println(this.tableList);
    }
}
