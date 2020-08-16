<h1 align="center">ChartMaker</h1>

<p align="center">
  <a href="https://android-arsenal.com/api?level=16"><img alt="API" src="https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat"/></a>
</p>

<p align="center">  
Create pie charts, simple and quick.</br>
ChartMaker was made to try out a modern approach to develop Android apps.
</p>
</br>

<p align="center">
<img src="/screenshots/preview.png"/>
</p>

## Download
Download it from [Google Play](https://play.google.com/store/apps/details?id=com.stokerapps.chartmaker)

## Architecture
ChartMaker:
- Is 100% written in [Kotlin](https://kotlinlang.org/)
- Is developed for Android API 16+, so older devices are also supported
- Uses the [MVVM](https://github.com/android/architecture-samples/tree/todo-mvvm-live-kotlin) architecture, but borrows simplified view states from MVI
- Is divided into 3 layers:
  - Data: Uses the repository pattern to manage data. A [Room](https://developer.android.com/topic/libraries/architecture/room) database stores data locally
  - Domain: Hosts domain models. Use cases are only used for complex or repetitive user actions
  - UI: A single-activity architecture with [Navigation](https://developer.android.com/guide/navigation) is used. Reactive views using [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) observables and [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- Uses [Paging](https://developer.android.com/topic/libraries/architecture/paging) to lazily load charts
- Uses [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) for background operations
- Uses [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html) to fetch and observe data streams
- Uses [Koin](https://github.com/InsertKoinIO/koin) to manage its dependencies
- Uses [Espresso](https://developer.android.com/training/testing/espresso) for UI and end-to-end tests

## Used Libraries
- [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) to actually display the charts
- [Timber](https://github.com/JakeWharton/timber) for logging