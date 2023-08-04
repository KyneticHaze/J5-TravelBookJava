package com.furkanharmanci.travelbookjava.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.furkanharmanci.travelbookjava.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlaceDao {
    @Query("SELECT * FROM Place")
    Flowable<List<Place>> getAll();
    //Query'lerde Single veya FLowable kullanıyoruz

    @Insert
    Completable insert(Place place);
    //Completable => Tamamlanacak bir işlemde kullanılır. Bir şey beklemez(veri)

    @Delete
    Completable delete(Place place);
}
