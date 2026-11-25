package com.covoitall.mobile.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReservationDao_Impl implements ReservationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReservationEntity> __insertionAdapterOfReservationEntity;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public ReservationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReservationEntity = new EntityInsertionAdapter<ReservationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `my_reservations` (`id`,`rideId`,`seats`,`amount`,`status`,`paymentStatus`,`createdAt`,`origin`,`destination`,`departureAt`,`driverName`,`rideStatus`,`pricePerSeat`,`rideSeatsAvailable`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReservationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getRideId());
        statement.bindLong(3, entity.getSeats());
        statement.bindDouble(4, entity.getAmount());
        statement.bindString(5, entity.getStatus());
        statement.bindString(6, entity.getPaymentStatus());
        statement.bindString(7, entity.getCreatedAt());
        statement.bindString(8, entity.getOrigin());
        statement.bindString(9, entity.getDestination());
        statement.bindString(10, entity.getDepartureAt());
        statement.bindString(11, entity.getDriverName());
        statement.bindString(12, entity.getRideStatus());
        statement.bindDouble(13, entity.getPricePerSeat());
        statement.bindLong(14, entity.getRideSeatsAvailable());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM my_reservations WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM my_reservations";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final ReservationEntity reservation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReservationEntity.insert(reservation);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<ReservationEntity> reservations,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReservationEntity.insert(reservations);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object replaceAll(final List<ReservationEntity> reservations,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ReservationDao.DefaultImpls.replaceAll(ReservationDao_Impl.this, reservations, __cont), $completion);
  }

  @Override
  public Object delete(final String reservationId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, reservationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ReservationEntity>> observeAll() {
    final String _sql = "SELECT * FROM my_reservations ORDER BY departureAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"my_reservations"}, new Callable<List<ReservationEntity>>() {
      @Override
      @NonNull
      public List<ReservationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRideId = CursorUtil.getColumnIndexOrThrow(_cursor, "rideId");
          final int _cursorIndexOfSeats = CursorUtil.getColumnIndexOrThrow(_cursor, "seats");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfPaymentStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStatus");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOrigin = CursorUtil.getColumnIndexOrThrow(_cursor, "origin");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfDepartureAt = CursorUtil.getColumnIndexOrThrow(_cursor, "departureAt");
          final int _cursorIndexOfDriverName = CursorUtil.getColumnIndexOrThrow(_cursor, "driverName");
          final int _cursorIndexOfRideStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rideStatus");
          final int _cursorIndexOfPricePerSeat = CursorUtil.getColumnIndexOrThrow(_cursor, "pricePerSeat");
          final int _cursorIndexOfRideSeatsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "rideSeatsAvailable");
          final List<ReservationEntity> _result = new ArrayList<ReservationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ReservationEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpRideId;
            _tmpRideId = _cursor.getString(_cursorIndexOfRideId);
            final int _tmpSeats;
            _tmpSeats = _cursor.getInt(_cursorIndexOfSeats);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final String _tmpPaymentStatus;
            _tmpPaymentStatus = _cursor.getString(_cursorIndexOfPaymentStatus);
            final String _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getString(_cursorIndexOfCreatedAt);
            final String _tmpOrigin;
            _tmpOrigin = _cursor.getString(_cursorIndexOfOrigin);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpDepartureAt;
            _tmpDepartureAt = _cursor.getString(_cursorIndexOfDepartureAt);
            final String _tmpDriverName;
            _tmpDriverName = _cursor.getString(_cursorIndexOfDriverName);
            final String _tmpRideStatus;
            _tmpRideStatus = _cursor.getString(_cursorIndexOfRideStatus);
            final double _tmpPricePerSeat;
            _tmpPricePerSeat = _cursor.getDouble(_cursorIndexOfPricePerSeat);
            final int _tmpRideSeatsAvailable;
            _tmpRideSeatsAvailable = _cursor.getInt(_cursorIndexOfRideSeatsAvailable);
            _item = new ReservationEntity(_tmpId,_tmpRideId,_tmpSeats,_tmpAmount,_tmpStatus,_tmpPaymentStatus,_tmpCreatedAt,_tmpOrigin,_tmpDestination,_tmpDepartureAt,_tmpDriverName,_tmpRideStatus,_tmpPricePerSeat,_tmpRideSeatsAvailable);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
