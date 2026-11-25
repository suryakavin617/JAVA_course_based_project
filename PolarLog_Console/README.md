# PolarLog - Console Edition

Integrated Polar Expedition Logistics and Asset Management System
Java Course Based Project (console version, no GUI yet)

Physical devices (tags, GPS, satellite link) are **simulated** by a `DeviceSimulator` thread.

## How to run

You need a JDK (version 8 or above).

**Windows:** double-click or run `compile.bat`, then `run.bat`
**Linux / macOS:** `./compile_and_run.sh`
**Manual:**
```
javac -d out src/com/polarlog/*/*.java
java -cp out com.polarlog.app.Main
```

Command line arguments: `--demo` (load sample data), `--reset` (delete saved data), `--help`.
On the first run the program asks whether to load sample demo data. Answer `y`.

Files created in the `data` folder:

| File | Purpose |
|------|---------|
| `polarlog.dat` | all data, saved with serialization when you exit |
| `command_centre_log.txt` | records "sent" to the command centre by the sync thread |
| `report.txt` | the report generated from menu 8 |

## Five-minute demo (after loading the demo data)

1. Menu 7: dashboard. The link starts **OFFLINE** with 25 updates queued.
2. Menu 1 -> 2: try to change asset 1 from REGISTERED to IN_USE. The rule is refused. Change it to DEPLOYED instead.
3. Menu 2 -> 4: remove 3 of item 4 (Fuel Drum); then try 99 to see `InsufficientStockException`.
4. Menu 5 -> 1: raise an SOS (member ID 2). It is sent immediately, even though the link is offline.
5. Menu 5 -> 2, 3, 4: acknowledge, dispatch and close it with operator ID 1. Try to close it again.
6. Menu 6 -> 2: look at the pending queue. Menu 6 -> 1: switch the link ONLINE and watch the `[SYNC]` lines.
7. Menu 6 -> 3: start the simulator, wait ~10 s for `[SIM]` events, then menu 6 -> 5 to see thread states.
8. Menu 8 -> 1, 2, 3: generate and view the report and the command centre log. Menu 0 saves and exits.

Operator ID is 1, members are 2-4, assets 1-5, inventory items 1-4, expedition 1 (in the demo data).

## Where each syllabus topic is used

| Unit | Topic | Where in the project |
|------|-------|----------------------|
| I | Object, class, encapsulation | all classes in `model`; private fields with getters |
| I | Inheritance, polymorphism | `Person` -> `StationMember`, `CommandOperator`; `Alert` -> `SOSAlert`, `LowStockAlert`; overriding `getRole()`, `getPriority()`; overloading `AssetService.searchAsset(int)` / `searchAsset(String)` |
| I | Abstract classes | `Person`, `Alert` |
| I | Interfaces: define, extend, implement | `Identifiable` <- `Trackable` (extends) <- `Asset` (implements); `Reportable` with a default method; also `Comparable`, `Serializable`, `Runnable` |
| I | Nested classes | `Asset.LocationRecord` (static nested), `ConsoleUI.MenuAction`, `ConsoleUI.InputClosedException` |
| I | Lambda, functional interfaces | `AssetFilter` (`@FunctionalInterface`), lambdas in `ConsoleUI.filterAssets`, `DeviceSimulator.pickAsset`, `AssetService.filter`, `ConsoleUI` menu actions; `Predicate` in `Repository`; method reference `InventoryItem::isLowStock` |
| I | Packages | `com.polarlog.model / exception / dao / sync / service / sim / util / app` |
| I | Command line arguments, arrays | `Main` (`--demo`, `--reset`, `--help`); `PLACES` array, `AssetStatus.values()` |
| II | Exception handling | 4 user-defined exceptions (checked and unchecked); multiple catch and `finally` in `ConsoleUI.execute()`; `try-with-resources` in `FileStorage` |
| II | Threads: Thread class, Runnable | `SyncManager extends Thread`; `DeviceSimulator implements Runnable` |
| II | Life cycle, priority | menu 6 -> 5 shows NEW, RUNNABLE, WAITING, TIMED_WAITING, TERMINATED; `setPriority()` in `SyncManager` and in `ConsoleUI.startSimulator()` |
| II | Synchronization, inter-thread communication | `synchronized` methods in `SyncQueue`, `Repository`, `Asset`, `AssetService`; `wait()` / `notifyAll()` in `SyncQueue` |
| III | java.io: files, byte and character streams, serialization | `FileStorage` (`ObjectOutputStream` / `ObjectInputStream`, `BufferedWriter`, `BufferedReader`, `File`) |
| III | java.lang: Object, wrapper classes, String, StringBuffer, System | `equals` / `hashCode` / `toString` in `Asset`; `Integer.parseInt`, autoboxing; `StringBuffer` in reports; `System.currentTimeMillis()`, `getProperty()`, `lineSeparator()` |
| III | java.util: Scanner, StringTokenizer, regex | `Scanner` in `ConsoleUI`; `StringTokenizer` in `InventoryService.quickAdd`; `Pattern` / `Matcher` in `Validator` |
| III | java.time | `DateUtil`, `Asset.LocationRecord` (`LocalDateTime`), `Expedition` (`LocalDate`, `ChronoUnit`) |
| IV | Generics, wildcards, generic methods | `Repository<T extends Identifiable>`; `CollectionUtil.sortedCopy`, `findMax`; `Collection<? extends Reportable>`, `Collection<?>` |
| IV | Collections: List, Set, Queue, Deque, Map | `ArrayList`, `LinkedList` (movement log); `TreeSet`, `HashSet`; `PriorityQueue` (`SyncQueue`, `AlertService`); `ArrayDeque` (`ActivityLog`); `HashMap`, `TreeMap` |
| IV | Traversing, sorting, searching | for-each and `Iterator` (`ConsoleUI.viewInventory`); `Collections.sort`; `Collections.binarySearch` (`AssetService.searchAsset(String)`); `Collections.max` |
| V | GUI (AWT, Swing, JavaFX) | not included in this console version; the `service` layer is ready to be connected to a JavaFX screen later |

## Project structure

```
src/com/polarlog/
  model/      Asset, Person, StationMember, CommandOperator, InventoryItem, Expedition,
              Alert, SOSAlert, LowStockAlert, SyncRecord, MovementLog, enums, interfaces
  exception/  InsufficientStockException, InvalidStatusChangeException,
              ItemNotFoundException, InvalidInputException
  dao/        Repository<T>, DataStore, FileStorage
  sync/       SyncQueue, SyncManager (thread)
  service/    AssetService, InventoryService, PersonnelService, ExpeditionService,
              AlertService, DashboardService, ActivityLog, AssetFilter
  sim/        DeviceSimulator (thread)
  util/       Validator, DateUtil, CollectionUtil
  app/        Main, ConsoleUI, DemoData
```

## Design notes

* Alert status moves only forward: RAISED, ACKNOWLEDGED, DISPATCHED, CLOSED.
* SOS records have priority 1 and are sent first, even when the normal link is offline.
* Other updates are queued while offline and sent in priority order when the link returns.
* Unsent updates are saved on exit and sent the next time the program starts.
* Data is stored with Java serialization (files), because JDBC is not in the syllabus.
