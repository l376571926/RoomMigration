package group.tonight.roommigrationdemo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Room Migration Sql 语句助手(仅适用于未指定表名的实体)
 * Author：376571926
 * Date: 2019/1/16 0016 17:34
 */
public class RoomMigrationSqlHelper {
    /**
     * 添加表
     * sql在app/schemas/3.json中复制createSql
     *
     * @param tableClass 要添加表的实体类
     * @param createSql  CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT, `address` TEXT)
     * @return
     */
    public static String addTable(Class<?> tableClass, String createSql) {
        return createSql.replace("`${TABLE_NAME}`", tableClass.getSimpleName());
    }

    /**
     * 删除表
     *
     * @param tableClass 要删除表的实体类
     * @return
     */
    public static String deleteTable(Class<?> tableClass) {
        return "DROP TABLE " + tableClass.getSimpleName();
    }

    /**
     * 添加 int 类型表字段
     *
     * @param tableClass
     * @param fieldName
     * @return
     */
    public static String addIntegerColumn(Class<?> tableClass, String fieldName) {
        //https://www.jianshu.com/p/41272f319ae7
        //java.lang.IllegalStateException: Migration didn't properly handle Pig(group.tonight.roomdatabasedemo.model.Pig).
        ////当添加int类型数据时，需要添加默认值
        return "ALTER TABLE " + tableClass.getSimpleName() + " ADD COLUMN " + fieldName + " INTEGER NOT NULL DEFAULT 0";
    }

    /**
     * 添加 String 类型的表字段
     *
     * @param tableClass
     * @param fieldName
     * @return
     */
    public static String addTextColumn(Class<?> tableClass, String fieldName) {
        return "ALTER TABLE " + tableClass.getSimpleName() + " ADD COLUMN " + fieldName + " TEXT";
    }

    /**
     * 删除表字段
     *
     * @param tableClass
     * @param fieldName
     * @return
     */
    /**
     * @param tableClass
     * @param createSql  CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT, `age` INTEGER NOT NULL, `address` TEXT)
     * @return
     */
    public static String[] deleteTableColumn(Class<?> tableClass, String createSql) {
        String tempTableName = "tempTableName";
        String newTableName = tableClass.getSimpleName();

        //1.新建临时表，sql语句从app/schemas/*.json中复制
        String sql1 = createSql.replace("`${TABLE_NAME}`", tempTableName);

        //2.将原表中的数据复制到临时表中,不复制要删除的字段的值
        String string = Arrays.toString(RoomMigrationSqlHelper.getAllFieldName(tableClass).toArray(new String[0]));
        String currentFiledName = string.substring(1, string.length() - 1);
        String sql2 = "INSERT INTO " + tempTableName + " (" + currentFiledName + ") SELECT " + currentFiledName + " FROM " + newTableName;

        //3.删除旧表
        String sql3 = "DROP TABLE " + newTableName;

        //4.重命名临时表为原表名称
        String sql4 = "ALTER TABLE " + tempTableName + " RENAME TO " + newTableName;
        return new String[]{
                sql1
                , sql2
                , sql3
                , sql4
        };
    }

    /**
     * 获取指定类所有成员变量名称
     *
     * @param tableClass
     * @return
     */
    public static List<String> getAllFieldName(Class<?> tableClass) {
        List<String> fieldNameList = new ArrayList<>();
        Field[] declaredFields = tableClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            String name = declaredField.getName();
            if ("serialVersionUID".equals(name)) {
                continue;
            }
            if ("$change".equals(name)) {
                continue;
            }
            fieldNameList.add(name);
        }
        return fieldNameList;
    }
}
