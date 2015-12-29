# Pulsera SPC Fit Pro (9602N)

SPC Fit Pro es una pulsera fitness que te permite monitorizar tu actividad física diaria.
- Se puede establecer un objetivo.
- Guarda información personal.
- Cuenta los pasos, las calorías quemadas y la distancia recorrida.
- Monitoriza la actividad en modo sueño.

### Objetivo
Este dispositivo es capaz de guardar un objetivo en pasos y mostrar un porcentaje de progreso en la pantalla.

### Información personal
La pulsera es capaz de almacenar información personal que será utilizada para realizar los cálculos de las calorías quemadas y la distancia recorrida en base a los pasos realizados. Es importante establecer estos parámetros para que la información sea lo mas certera posible.

### Pasos, calorías y distancia
La pulsera guarda los pasos y calcula las calorías y la distancia recorrida así como los pasos aeróbicos (siempre 0) y el tiempo de actividad.

### Sueño
La pulsera es capaz de cambiar de modo normal a modo sueño y viceversa presionando la pantalla durante 3 segundos y pulsando de nuevo cuando aparezca la opción de cambio de estado.

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
Al activarse el modo en vivo, la pulsera envía los pasos, los pasos aeróbicos (siempre 0), las calorías, la distancia y el tiempo de actividad cada vez que detecta un movimiento.

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
La pulsera tiene guardado un código identificativo, el formato es `A0YM1000NNNN8380671`:
-`A0` hace referencia al modelo.
-`Y` hace referencia al ultimo dígito del año en el que se fabrico (2015 = 5).
-`M` hace referencia al mes en el que se fabricó expresado en Hexadecimal (octubre = A).
-`NNNN` hace referencia al número único de dispositivo.
#### Número de serie: 
En la caja, en la base y en el dispositivo, en la parte posterior podemos encontrar el número de serie, el formato es `9602NYYMMC11NNNN`
-`9602N` hace referencia al modelo.
-`YY` hace referencia a los dos últimos dígitos del año en el que se fabrico (2015 = 15).
-`MM` hace referencia al mes en el que se fabrico (junio = 06).
-`NNNN` hace referencia al numero único de dispositivo.