package group.tonight.roommigrationdemo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import group.tonight.roommigrationdemo.model.Cat;

@Dao
public interface CatDao {
    @Insert
    void insert(Cat cat);
}
