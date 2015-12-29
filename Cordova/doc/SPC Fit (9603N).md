# Pulsera SPC Fit Pro (9603N)

SPC Fit Pro es una pulsera fitness que te permite monitorizar tu actividad física diaria.
- Para mayor seguridad este dispositivo se conecta usando una contraseña.
- Se puede establecer un objetivo.
- Guarda información personal.
- Cuenta los pasos, las calorísas quemadas y la distancia recorrida.
- Monitoriza la actividad en modo sueño.

### Conexión
A la hora de conectarse, este dispositivo requiere de una contraseña para realizar un emparejamiento válido. En caso de querer realizar por primera vez el emparejamiento se le proporciona la contraseña y se tiene que mover la pulsera antes de 10 segundos un par de veces.

### Objetivo
Este dispositivo es capaz de guardar un objetivo en pasos.

### Información personal
La pulsera es capaz de almacenar información personal que será utilizada para realizar los cálculos de las calorías quemadas y la distancia recorrida en base a los pasos realizados. Es importante establecer estos parámetros para que la información sea lo mas certera posible.

### Pasos, calorías y distancia
La pulsera guarda los pasos y calcula las calorias y la distancia recorrida asi como los pasos aeróbicos (siempre 0) y el tiempo de actividad.

### Sueño
La pulsera es capaz de cambiar de modo normal a modo sueño y viceversa enviandole un comando mediante una aplicación.

## Gestión de la información

### ¿Que datos le puedo preguntar a la pulsera y como los devuelve?

#### Fecha:
Devuelve la fecha actual de la pulsera.

#### Objetivo:
Devuelve el objetivo establecido actualmente en la pulsera.

#### Información personal:
Devuelve los datos personales almacenados en la pulsera, los cuales son el sexo, la edad, la altura, el peso y la longitud de paso.

#### Información total de la actividad actual:
La pulsera devuelve los pasos, los pasos aeróbicos (siempre 0), las calorías, la distancia y el tiempo de actividad de la actividad actual. 

#### Información en vivo:
Al activarse el modo en vivo, la pulsera envia los pasos, los pasos aeróbicos (siempre 0), las calorías, la distancia y el tiempo de actividad cada vez que detecta un movimiento.

#### Información total de un día:
La pulsera devuelve la fecha, los pasos, los pasos aeróbicos (siempre 0), las calorías y la distancia de un día en concreto. Tiene una capacidad de 30 registros, si en un día se detecta actividad este queda registrado, borrándose el día mas antiguo del registro. Dependiendo de la cantidad de información que tenga que guardar la pulsera puede guardar la información de un día en 2 registros.

#### Información detallada de un día:
El tratamiento de estos datos es especial, al preguntarle por la información detallada de un día el dispositivo devuelve el día fragmentado en momentos de 15 minutos, estos momentos pueden ser de información de actividad o de sueño dependiendo del estado en el que se encontrara la pulsera.

Si la información detallada es de hoy hay que tener en cuenta que hasta que no se acabe el momento el dispositivo no devolverá la información real en ese fragmento.

Ej. Si son las 8:43 la información relativa al momento 8:30 día 0, sin embargo cuando sean las 8:46 la actividad realizada entre las 8:30 y las 8:45 se vera reflejada en el fragmento de las 8:30.

- Fragmentos de actividad: Los fragmentos de actividad contienen fecha, pasos, pasos aeróbicos ( siempre 0), calorías y distancia.

- Fragmentos de sueño: Un fragmento de sueño se divide en 8 fragmentos mas pequeños, los siete primeros corresponden a fragmentos de 2 minutos y el ultimo de un minuto. Estos fragmentos mas pequeños contienen fecha y la calidad. La calidad de sueño es la cantidad de veces que se ha movido la pulsera en ese lapso de tiempo dividido entre 2, con un máximo de 11, es decir, si se ha movido mas de 22 veces o mas la calidad es 11.

### ¿Como identifico mi pulsera?
#### Código identificativo: 
La pulsera tiene guardado un código identificativo, el formato es `A1YM1000NNNN8380671`:
-`A1` hace referencia al modelo.c
-`NNNN` hace referencia al número único de dispositivo.
#### Número de serie: 
En la caja, en la base y en el dispositivo, en la parte posterior podemos encontrar el número de serie, el formato es `9603NYYMMC11NNNN`
-`9603N` hace referencia al modelo.
-`YY` hace referencia a los dos últimos dígitos del año en el que se fabricó (2015 = 15).
-`MM` hace referencia al mes en el que se fabricó (junio = 06).
-`NNNN` hace referencia al numero único de dispositivo.