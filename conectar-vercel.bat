@echo off
title Conectar NiHao Lab a Vercel
echo ============================================
echo   NiHao Lab - conexion a Vercel (3 pasos)
echo ============================================
echo.
echo [1/3] Login: se abrira tu navegador para confirmar.
call "%~dp0node_modules\.bin\vercel.cmd" login
if errorlevel 1 (
  echo Login cancelado.
  pause
  exit /b 1
)
echo.
echo [2/3] Vinculando el proyecto...
call "%~dp0node_modules\.bin\vercel.cmd" link --yes
if errorlevel 1 (
  pause
  exit /b 1
)
echo.
echo [3/3] Desplegando a produccion...
call "%~dp0node_modules\.bin\vercel.cmd" deploy --prod --yes
echo.
echo ============================================
echo   Listo: arriba tienes tu URL permanente.
echo   Para futuros cambios: doble clic en este
echo   bat o ejecuta  npm run deploy
echo ============================================
pause
