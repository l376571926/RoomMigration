package group.tonight.roommigrationdemo.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;


import java.util.List;

import group.tonight.roommigrationdemo.model.Word;


@Dao
public interface WordDao {
    @Insert
    void insert(Word word);

    @Query("select * from word")
    LiveData<List<Word>> loadAll();

    @Delete
    void delete(Word word);
}
