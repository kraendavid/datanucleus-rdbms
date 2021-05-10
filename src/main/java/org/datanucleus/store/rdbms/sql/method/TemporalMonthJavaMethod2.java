/**********************************************************************
Copyright (c) 2016 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus.store.rdbms.sql.method;

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.store.rdbms.mapping.java.JavaTypeMapping;
import org.datanucleus.store.query.expression.Expression;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.sql.SQLStatement;
import org.datanucleus.store.rdbms.sql.expression.ExpressionUtils;
import org.datanucleus.store.rdbms.sql.expression.NumericExpression;
import org.datanucleus.store.rdbms.sql.expression.SQLExpression;
import org.datanucleus.store.rdbms.sql.expression.SQLExpressionFactory;
import org.datanucleus.store.rdbms.sql.expression.StringExpression;

/**
 * Method for evaluating {dateExpr}.getMonth().
 * Returns a NumericExpression that equates to <pre>TO_NUMBER(TO_CHAR(dateExpr, "MM")-1)</pre>
 */
public class TemporalMonthJavaMethod2 extends TemporalBaseMethod
{
    /* (non-Javadoc)
     * @see org.datanucleus.store.rdbms.sql.method.SQLMethod#getExpression(org.datanucleus.store.rdbms.sql.expression.SQLExpression, java.util.List)
     */
    public SQLExpression getExpression(SQLStatement stmt, SQLExpression expr, List<SQLExpression> args)
    {
        SQLExpression invokedExpr = getInvokedExpression(expr, args, "MONTH_JAVA");

        SQLExpression one = ExpressionUtils.getLiteralForOne(stmt);
        RDBMSStoreManager storeMgr = stmt.getRDBMSManager();
        JavaTypeMapping mapping2 = storeMgr.getMappingManager().getMapping(String.class);
        SQLExpressionFactory exprFactory = stmt.getSQLExpressionFactory();
        SQLExpression mm = exprFactory.newLiteral(stmt, mapping2, "MM");

        ArrayList funcArgs = new ArrayList();
        funcArgs.add(invokedExpr);
        funcArgs.add(mm);
        ArrayList funcArgs2 = new ArrayList();
        funcArgs2.add(new StringExpression(stmt, stmt.getSQLExpressionFactory().getMappingForType(int.class, true), "TO_CHAR", funcArgs));

        // Delete one from the SQL "month" (origin=1) to be compatible with Java month (origin=0)
        NumericExpression numExpr = new NumericExpression(new NumericExpression(stmt, stmt.getSQLExpressionFactory().getMappingForType(int.class, true), 
            "TO_NUMBER", funcArgs2), Expression.OP_SUB, one);
        numExpr.encloseInParentheses();
        return numExpr;
    }
}
