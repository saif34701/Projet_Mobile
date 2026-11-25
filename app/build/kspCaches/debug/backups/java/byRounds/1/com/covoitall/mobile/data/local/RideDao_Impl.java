package com.covoitall.mobile.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
public final class RideDao_Impl implements RideDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RideEntity> __insertionAdapterOfRideEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public RideDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRideEntity = new EntityInsertionAdapter<RideEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `my_rides` (`id`,`origin`,`destination`,`departureAt`,`seatsTotal`,`seatsReserved`,`seatsAvailable`,`pendingRequests`,`price`,`rideType`,`recurrenceDays`,`acceptanceMode`,`status`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RideEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getOrigin());
        statement.bindString(3, entity.getDestination());
        statement.bindString(4, entity.getDepartureAt());
        statement.bindLong(5, entity.getSeatsTotal());
        statement.bindLong(6, entity.getSeatsReserved());
        statement.bindLong(7, entity.getSeatsAvailable());
        statement.bindLong(8, entity.getPendingRequests());
        statement.bindDouble(9, entity.getPrice());
        statement.bindString(10, entity.getRideType());
        final String _tmp = __converters.fromList(entity.getRecurrenceDays());
        statement.bindString(11, _tmp);
        statement.bindString(12, entity.getAcceptanceMode());
        statement.bindString(13, entity.getStatus());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM my_rides WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM my_rides";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final RideEntity ride, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRideEntity.insert(ride);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<RideEntity> rides,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfRideEntity.insert(rides);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object replaceAll(final List<RideEntity> rides,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> RideDao.DefaultImpls.replaceAll(RideDao_Impl.this, rides, __cont), $completion);
  }

  @Override
  public Object delete(final String rideId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, rideId);
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
  public Flow<List<RideEntity>> observeAll() {
    final String _sql = "SELECT * FROM my_rides ORDER BY departureAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"my_rides"}, new Callable<List<RideEntity>>() {
      @Override
      @NonNull
      public List<RideEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOrigin = CursorUtil.getColumnIndexOrThrow(_cursor, "origin");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfDepartureAt = CursorUtil.getColumnIndexOrThrow(_cursor, "departureAt");
          final int _cursorIndexOfSeatsTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsTotal");
          final int _cursorIndexOfSeatsReserved = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsReserved");
          final int _cursorIndexOfSeatsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsAvailable");
          final int _cursorIndexOfPendingRequests = CursorUtil.getColumnIndexOrThrow(_cursor, "pendingRequests");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfRideType = CursorUtil.getColumnIndexOrThrow(_cursor, "rideType");
          final int _cursorIndexOfRecurrenceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurrenceDays");
          final int _cursorIndexOfAcceptanceMode = CursorUtil.getColumnIndexOrThrow(_cursor, "acceptanceMode");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<RideEntity> _result = new ArrayList<RideEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RideEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOrigin;
            _tmpOrigin = _cursor.getString(_cursorIndexOfOrigin);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpDepartureAt;
            _tmpDepartureAt = _cursor.getString(_cursorIndexOfDepartureAt);
            final int _tmpSeatsTotal;
            _tmpSeatsTotal = _cursor.getInt(_cursorIndexOfSeatsTotal);
            final int _tmpSeatsReserved;
            _tmpSeatsReserved = _cursor.getInt(_cursorIndexOfSeatsReserved);
            final int _tmpSeatsAvailable;
            _tmpSeatsAvailable = _cursor.getInt(_cursorIndexOfSeatsAvailable);
            final int _tmpPendingRequests;
            _tmpPendingRequests = _cursor.getInt(_cursorIndexOfPendingRequests);
            final double _tmpPrice;
            _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            final String _tmpRideType;
            _tmpRideType = _cursor.getString(_cursorIndexOfRideType);
            final List<String> _tmpRecurrenceDays;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRecurrenceDays);
            _tmpRecurrenceDays = __converters.toList(_tmp);
            final String _tmpAcceptanceMode;
            _tmpAcceptanceMode = _cursor.getString(_cursorIndexOfAcceptanceMode);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new RideEntity(_tmpId,_tmpOrigin,_tmpDestination,_tmpDepartureAt,_tmpSeatsTotal,_tmpSeatsReserved,_tmpSeatsAvailable,_tmpPendingRequests,_tmpPrice,_tmpRideType,_tmpRecurrenceDays,_tmpAcceptanceMode,_tmpStatus);
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

  @Override
  public Object byId(final String rideId, final Continuation<? super RideEntity> $completion) {
    final String _sql = "SELECT * FROM my_rides WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, rideId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RideEntity>() {
      @Override
      @Nullable
      public RideEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOrigin = CursorUtil.getColumnIndexOrThrow(_cursor, "origin");
          final int _cursorIndexOfDestination = CursorUtil.getColumnIndexOrThrow(_cursor, "destination");
          final int _cursorIndexOfDepartureAt = CursorUtil.getColumnIndexOrThrow(_cursor, "departureAt");
          final int _cursorIndexOfSeatsTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsTotal");
          final int _cursorIndexOfSeatsReserved = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsReserved");
          final int _cursorIndexOfSeatsAvailable = CursorUtil.getColumnIndexOrThrow(_cursor, "seatsAvailable");
          final int _cursorIndexOfPendingRequests = CursorUtil.getColumnIndexOrThrow(_cursor, "pendingRequests");
          final int _cursorIndexOfPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "price");
          final int _cursorIndexOfRideType = CursorUtil.getColumnIndexOrThrow(_cursor, "rideType");
          final int _cursorIndexOfRecurrenceDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurrenceDays");
          final int _cursorIndexOfAcceptanceMode = CursorUtil.getColumnIndexOrThrow(_cursor, "acceptanceMode");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final RideEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpOrigin;
            _tmpOrigin = _cursor.getString(_cursorIndexOfOrigin);
            final String _tmpDestination;
            _tmpDestination = _cursor.getString(_cursorIndexOfDestination);
            final String _tmpDepartureAt;
            _tmpDepartureAt = _cursor.getString(_cursorIndexOfDepartureAt);
            final int _tmpSeatsTotal;
            _tmpSeatsTotal = _cursor.getInt(_cursorIndexOfSeatsTotal);
            final int _tmpSeatsReserved;
            _tmpSeatsReserved = _cursor.getInt(_cursorIndexOfSeatsReserved);
            final int _tmpSeatsAvailable;
            _tmpSeatsAvailable = _cursor.getInt(_cursorIndexOfSeatsAvailable);
            final int _tmpPendingRequests;
            _tmpPendingRequests = _cursor.getInt(_cursorIndexOfPendingRequests);
            final double _tmpPrice;
            _tmpPrice = _cursor.getDouble(_cursorIndexOfPrice);
            final String _tmpRideType;
            _tmpRideType = _cursor.getString(_cursorIndexOfRideType);
            final List<String> _tmpRecurrenceDays;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRecurrenceDays);
            _tmpRecurrenceDays = __converters.toList(_tmp);
            final String _tmpAcceptanceMode;
            _tmpAcceptanceMode = _cursor.getString(_cursorIndexOfAcceptanceMode);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _result = new RideEntity(_tmpId,_tmpOrigin,_tmpDestination,_tmpDepartureAt,_tmpSeatsTotal,_tmpSeatsReserved,_tmpSeatsAvailable,_tmpPendingRequests,_tmpPrice,_tmpRideType,_tmpRecurrenceDays,_tmpAcceptanceMode,_tmpStatus);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
