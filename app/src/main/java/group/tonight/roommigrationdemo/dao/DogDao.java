package group.tonight.roommigrationdemo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

import group.tonight.roommigrationdemo.model.Dog;

@Dao
public interface DogDao {
    @Insert
    void insert(Dog dog);
}
