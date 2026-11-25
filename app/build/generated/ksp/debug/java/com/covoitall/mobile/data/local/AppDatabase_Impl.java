package com.covoitall.mobile.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SessionDao _sessionDao;

  private volatile NetworkDao _networkDao;

  private volatile RideDao _rideDao;

  private volatile ReservationDao _reservationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `session_user` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT NOT NULL, `role` TEXT NOT NULL, `driverStatus` TEXT, `token` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `lines` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `code` TEXT NOT NULL, `stopIds` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stops` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `address` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`id` TEXT NOT NULL, `lineId` TEXT NOT NULL, `departureTime` TEXT NOT NULL, `arrivalTime` TEXT NOT NULL, `days` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `my_rides` (`id` TEXT NOT NULL, `origin` TEXT NOT NULL, `destination` TEXT NOT NULL, `departureAt` TEXT NOT NULL, `seatsTotal` INTEGER NOT NULL, `seatsReserved` INTEGER NOT NULL, `seatsAvailable` INTEGER NOT NULL, `pendingRequests` INTEGER NOT NULL, `price` REAL NOT NULL, `rideType` TEXT NOT NULL, `recurrenceDays` TEXT NOT NULL, `acceptanceMode` TEXT NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `my_reservations` (`id` TEXT NOT NULL, `rideId` TEXT NOT NULL, `seats` INTEGER NOT NULL, `amount` REAL NOT NULL, `status` TEXT NOT NULL, `paymentStatus` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `origin` TEXT NOT NULL, `destination` TEXT NOT NULL, `departureAt` TEXT NOT NULL, `driverName` TEXT NOT NULL, `rideStatus` TEXT NOT NULL, `pricePerSeat` REAL NOT NULL, `rideSeatsAvailable` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9853dd425799ff7a4cb29ecfbadc4275')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `session_user`");
        db.execSQL("DROP TABLE IF EXISTS `lines`");
        db.execSQL("DROP TABLE IF EXISTS `stops`");
        db.execSQL("DROP TABLE IF EXISTS `schedules`");
        db.execSQL("DROP TABLE IF EXISTS `my_rides`");
        db.execSQL("DROP TABLE IF EXISTS `my_reservations`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSessionUser = new HashMap<String, TableInfo.Column>(8);
        _columnsSessionUser.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("firstName", new TableInfo.Column("firstName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("lastName", new TableInfo.Column("lastName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("phone", new TableInfo.Column("phone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("driverStatus", new TableInfo.Column("driverStatus", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSessionUser.put("token", new TableInfo.Column("token", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSessionUser = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSessionUser = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSessionUser = new TableInfo("session_user", _columnsSessionUser, _foreignKeysSessionUser, _indicesSessionUser);
        final TableInfo _existingSessionUser = TableInfo.read(db, "session_user");
        if (!_infoSessionUser.equals(_existingSessionUser)) {
          return new RoomOpenHelper.ValidationResult(false, "session_user(com.covoitall.mobile.data.local.UserEntity).\n"
                  + " Expected:\n" + _infoSessionUser + "\n"
                  + " Found:\n" + _existingSessionUser);
        }
        final HashMap<String, TableInfo.Column> _columnsLines = new HashMap<String, TableInfo.Column>(4);
        _columnsLines.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLines.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLines.put("code", new TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLines.put("stopIds", new TableInfo.Column("stopIds", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLines = new TableInfo("lines", _columnsLines, _foreignKeysLines, _indicesLines);
        final TableInfo _existingLines = TableInfo.read(db, "lines");
        if (!_infoLines.equals(_existingLines)) {
          return new RoomOpenHelper.ValidationResult(false, "lines(com.covoitall.mobile.data.local.LineEntity).\n"
                  + " Expected:\n" + _infoLines + "\n"
                  + " Found:\n" + _existingLines);
        }
        final HashMap<String, TableInfo.Column> _columnsStops = new HashMap<String, TableInfo.Column>(5);
        _columnsStops.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStops.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStops.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStops.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStops.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStops = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStops = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStops = new TableInfo("stops", _columnsStops, _foreignKeysStops, _indicesStops);
        final TableInfo _existingStops = TableInfo.read(db, "stops");
        if (!_infoStops.equals(_existingStops)) {
          return new RoomOpenHelper.ValidationResult(false, "stops(com.covoitall.mobile.data.local.StopEntity).\n"
                  + " Expected:\n" + _infoStops + "\n"
                  + " Found:\n" + _existingStops);
        }
        final HashMap<String, TableInfo.Column> _columnsSchedules = new HashMap<String, TableInfo.Column>(5);
        _columnsSchedules.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("lineId", new TableInfo.Column("lineId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("departureTime", new TableInfo.Column("departureTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("arrivalTime", new TableInfo.Column("arrivalTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSchedules.put("days", new TableInfo.Column("days", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSchedules = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSchedules = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSchedules = new TableInfo("schedules", _columnsSchedules, _foreignKeysSchedules, _indicesSchedules);
        final TableInfo _existingSchedules = TableInfo.read(db, "schedules");
        if (!_infoSchedules.equals(_existingSchedules)) {
          return new RoomOpenHelper.ValidationResult(false, "schedules(com.covoitall.mobile.data.local.ScheduleEntity).\n"
                  + " Expected:\n" + _infoSchedules + "\n"
                  + " Found:\n" + _existingSchedules);
        }
        final HashMap<String, TableInfo.Column> _columnsMyRides = new HashMap<String, TableInfo.Column>(13);
        _columnsMyRides.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("origin", new TableInfo.Column("origin", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("destination", new TableInfo.Column("destination", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("departureAt", new TableInfo.Column("departureAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("seatsTotal", new TableInfo.Column("seatsTotal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("seatsReserved", new TableInfo.Column("seatsReserved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("seatsAvailable", new TableInfo.Column("seatsAvailable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("pendingRequests", new TableInfo.Column("pendingRequests", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("price", new TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("rideType", new TableInfo.Column("rideType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("recurrenceDays", new TableInfo.Column("recurrenceDays", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("acceptanceMode", new TableInfo.Column("acceptanceMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyRides.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMyRides = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMyRides = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMyRides = new TableInfo("my_rides", _columnsMyRides, _foreignKeysMyRides, _indicesMyRides);
        final TableInfo _existingMyRides = TableInfo.read(db, "my_rides");
        if (!_infoMyRides.equals(_existingMyRides)) {
          return new RoomOpenHelper.ValidationResult(false, "my_rides(com.covoitall.mobile.data.local.RideEntity).\n"
                  + " Expected:\n" + _infoMyRides + "\n"
                  + " Found:\n" + _existingMyRides);
        }
        final HashMap<String, TableInfo.Column> _columnsMyReservations = new HashMap<String, TableInfo.Column>(14);
        _columnsMyReservations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("rideId", new TableInfo.Column("rideId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("seats", new TableInfo.Column("seats", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("paymentStatus", new TableInfo.Column("paymentStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("createdAt", new TableInfo.Column("createdAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("origin", new TableInfo.Column("origin", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("destination", new TableInfo.Column("destination", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("departureAt", new TableInfo.Column("departureAt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("driverName", new TableInfo.Column("driverName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("rideStatus", new TableInfo.Column("rideStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("pricePerSeat", new TableInfo.Column("pricePerSeat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMyReservations.put("rideSeatsAvailable", new TableInfo.Column("rideSeatsAvailable", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMyReservations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMyReservations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMyReservations = new TableInfo("my_reservations", _columnsMyReservations, _foreignKeysMyReservations, _indicesMyReservations);
        final TableInfo _existingMyReservations = TableInfo.read(db, "my_reservations");
        if (!_infoMyReservations.equals(_existingMyReservations)) {
          return new RoomOpenHelper.ValidationResult(false, "my_reservations(com.covoitall.mobile.data.local.ReservationEntity).\n"
                  + " Expected:\n" + _infoMyReservations + "\n"
                  + " Found:\n" + _existingMyReservations);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9853dd425799ff7a4cb29ecfbadc4275", "7166258085bdd1968049d536155af096");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "session_user","lines","stops","schedules","my_rides","my_reservations");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `session_user`");
      _db.execSQL("DELETE FROM `lines`");
      _db.execSQL("DELETE FROM `stops`");
      _db.execSQL("DELETE FROM `schedules`");
      _db.execSQL("DELETE FROM `my_rides`");
      _db.execSQL("DELETE FROM `my_reservations`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SessionDao.class, SessionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NetworkDao.class, NetworkDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RideDao.class, RideDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReservationDao.class, ReservationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SessionDao sessionDao() {
    if (_sessionDao != null) {
      return _sessionDao;
    } else {
      synchronized(this) {
        if(_sessionDao == null) {
          _sessionDao = new SessionDao_Impl(this);
        }
        return _sessionDao;
      }
    }
  }

  @Override
  public NetworkDao networkDao() {
    if (_networkDao != null) {
      return _networkDao;
    } else {
      synchronized(this) {
        if(_networkDao == null) {
          _networkDao = new NetworkDao_Impl(this);
        }
        return _networkDao;
      }
    }
  }

  @Override
  public RideDao rideDao() {
    if (_rideDao != null) {
      return _rideDao;
    } else {
      synchronized(this) {
        if(_rideDao == null) {
          _rideDao = new RideDao_Impl(this);
        }
        return _rideDao;
      }
    }
  }

  @Override
  public ReservationDao reservationDao() {
    if (_reservationDao != null) {
      return _reservationDao;
    } else {
      synchronized(this) {
        if(_reservationDao == null) {
          _reservationDao = new ReservationDao_Impl(this);
        }
        return _reservationDao;
      }
    }
  }
}
