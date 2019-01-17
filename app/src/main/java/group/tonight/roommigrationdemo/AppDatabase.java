package group.tonight.roommigrationdemo;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import group.tonight.roommigrationdemo.dao.DogDao;
import group.tonight.roommigrationdemo.dao.WordDao;
import group.tonight.roommigrationdemo.model.Cat;
import group.tonight.roommigrationdemo.model.Dog;
import group.tonight.roommigrationdemo.model.Pig;
import group.tonight.roommigrationdemo.model.Word;

/**
 * https://www.jianshu.com/p/fae0245cf384
 * 升级数据库正确步骤：
 * 1.在@Database的entityes中添加、删除、修改带@Entity注解的实体类
 * 2.修改数据库版本号
 * 3.创建Migration，并执行数据库变更所需sql语句，1和2分别代表上一个版本和新的版本
 * 4.把migration 添加到 Room database builder中
 */
@Database(
        entities = {
                Word.class
                , Dog.class
                , Pig.class
        }
        //如果表结构改了（删除、添加表，删除添加表字段），未修改version，会抛这个异常
        //java.lang.IllegalStateException: Room cannot verify the data integrity.
        // Looks like you've changed schema but forgot to update the version number.
        // You can simply fix this by increasing the version number.
        , version = 6
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WordDao wordDao();

    public abstract DogDao dogDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database")
                            /*
                             * 删除Cat表中的所有数据
                             * java.lang.IllegalStateException: A migration from 1 to 2 was required but not found.
                             * Please provide the necessary Migration path via RoomDatabase.Builder.addMigration(Migration ...)
                             * or allow for destructive migrations via one of the RoomDatabase.Builder.fallbackToDestructiveMigration* methods.
                             */
//                            .addMigrations(MIGRATION_1_2)
//                            .addMigrations(MIGRATION_2_3)
//                            .addMigrations(MIGRATION_3_4)
//                            .addMigrations(MIGRATION_4_5)
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    System.out.println();
                                }

                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    System.out.println();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 添加表
     */
    private static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String createSqlOfDog = "CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)";
            database.execSQL(RoomMigrationSqlHelper.addTable(Dog.class, createSqlOfDog));

            String createSqlOfCat = "CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)";
            database.execSQL(RoomMigrationSqlHelper.addTable(Cat.class, createSqlOfCat));
        }
    };

    /**
     * 删除表
     */
    private static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(RoomMigrationSqlHelper.deleteTable(Cat.class));
        }
    };

    /**
     * 添加列
     */
    private static Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String sql1 = RoomMigrationSqlHelper.addIntegerColumn(Dog.class, "age");
            String sql2 = RoomMigrationSqlHelper.addTextColumn(Dog.class, "address");
            database.execSQL(sql1);
            database.execSQL(sql2);
        }
    };

    /**
     * 删除列
     */
    private static Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String createSql = "CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `address` TEXT)";
            String[] sqlArr = RoomMigrationSqlHelper.deleteTableColumn(Dog.class, createSql);
            for (String sql : sqlArr) {
                database.execSQL(sql);
            }
        }
    };
}
