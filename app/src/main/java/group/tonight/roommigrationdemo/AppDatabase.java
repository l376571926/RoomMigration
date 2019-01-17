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
        }
        , version = 3
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
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            String createSqlOfDog = "CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)";
            database.execSQL(RoomMigrationSqlHelper.addTable(Dog.class, createSqlOfDog));

            String createSqlOfCat = "CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)";
            database.execSQL(RoomMigrationSqlHelper.addTable(Cat.class, createSqlOfCat));
        }
    };

    private static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(RoomMigrationSqlHelper.deleteTable(Cat.class));
        }
    };
}
