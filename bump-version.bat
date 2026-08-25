@echo off
setlocal EnableExtensions
chcp 65001 >nul

:: Assicurati di essere nella root del progetto.
pushd "%~dp0" >nul 2>&1
if errorlevel 1 (
    echo Errore: impossibile accedere alla cartella del progetto.
    pause
    exit /b 1
)

set "TARGET_SCRIPT=scripts\bump_version.py"
if not exist "%TARGET_SCRIPT%" (
    echo Errore: file non trovato: %TARGET_SCRIPT%
    popd
    pause
    exit /b 1
)

:: Verifica Python preferendo il launcher py -3, poi python.
set "PY_CMD="
where py >nul 2>&1
if not errorlevel 1 (
    py -3 -c "import sys" >nul 2>&1
    if not errorlevel 1 (
        set "PY_CMD=py -3"
    )
)

if not defined PY_CMD (
    where python >nul 2>&1
    if not errorlevel 1 (
        python -c "import sys" >nul 2>&1
        if not errorlevel 1 (
            set "PY_CMD=python"
        )
    )
)

if not defined PY_CMD (
    echo Errore: Python non e installato o non e nel PATH.
    echo Installa Python oppure configura il launcher "py".
    popd
    pause
    exit /b 1
)

set "AUTO_MODE=0"
if not "%~1"=="" set "AUTO_MODE=1"

:: Esegue lo script Python interattivo o parametrico.
%PY_CMD% "%TARGET_SCRIPT%" %*
set "EXIT_CODE=%ERRORLEVEL%"

popd
if "%AUTO_MODE%"=="0" pause
exit /b %EXIT_CODE%
