package com.mindskip.xzs.utils;

import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.policy.TableRenderPolicy;
import com.deepoove.poi.util.TableTools;

/**
 * 催缴通知书 明细表格的自定义渲染策略<br/>
 * 1. 填充费用列表 <br/>
 * @author zjh
 * @version
 */
public class DetailTablePolicy extends DynamicTableRenderPolicy {

    // 货品填充数据所在行数
    int feesStartRow = 1;

    @Override
    public void render(XWPFTable table, Object data) throws Exception {
        if (null == data) return;
        DetailData detailData = (DetailData) data;

        List<RowRenderData> fees = detailData.getFees();
        if (null != fees) {
            table.removeRow(feesStartRow);
            for (int i = 0; i < fees.size(); i++) {
                XWPFTableRow insertNewTableRow = table.insertNewTableRow(feesStartRow);
                for (int j = 0; j < 6; j++) insertNewTableRow.createCell();
                TableRenderPolicy.Helper.renderRow(table.getRow(feesStartRow), fees.get(i));
            }
        }
    }

}