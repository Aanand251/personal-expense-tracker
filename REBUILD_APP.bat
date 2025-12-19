@echo off
echo ============================================
echo   COMPLETE REBUILD - Personal Expense Tracker
echo ============================================
echo.

echo [1/4] Uninstalling app from device...
adb uninstall com.example.personalexpensetracker 2>nul
echo App uninstalled (if it was installed)
echo.

echo [2/4] Please CLOSE Android Studio now if it's open!
echo Press any key when Android Studio is closed...
pause >nul
echo.

echo [3/4] Cleaning build files...
if exist "app\build" rmdir /s /q "app\build" 2>nul
if exist ".gradle" rmdir /s /q ".gradle" 2>nul
if exist "build" rmdir /s /q "build" 2>nul
echo Build cache cleared!
echo.

echo [4/4] Building and installing fresh APK...
echo This may take 1-2 minutes...
echo.
call gradlew.bat clean assembleDebug installDebug

echo.
echo ============================================
if %ERRORLEVEL% EQU 0 (
    echo   BUILD SUCCESSFUL! App installed on device.
    echo   You can now run the app!
) else (
    echo   BUILD FAILED! Check errors above.
)
echo ============================================
pause
