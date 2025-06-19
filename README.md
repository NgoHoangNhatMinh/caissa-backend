# Chess Engine

This repo was migrated from https://github.com/NgoHoangNhatMinh/chess to host the engine as a backend instead of a standalone program.

## Getting Started

To start the CLI program:

```bash
make
```

To start the backend server locally, run:

```bash
./mvnw spring-boot:run
```

or start a docker container ([install docker](https://docs.docker.com/desktop/setup/install/windows-install/)) with

```bash
docker build -t caissa-engine .
docker run -p 8080:8080 caissa-engine
```

and access the endpoints at

```
http://localhost:8080
```

## CLI Gameplay

Enter moves using the format

```
e2e4 // for pawn moves
ke1e2 // king e1 to e2
e7e8_q // for promotion to queen
```

## CLI Testing

Move generations can be checked by performing a Perft tests. At each depth, count all possible legal moves generated and compare against some reputable known values.

To run Perft test

```bash
make test
```

or

```bash
make test DEPTH=#enter-depth-here
```

Here's the first few values from the initial positions
| Depth | Nodes |
|-------|-------|
| 1 | 20 |
| 2 | 400 |
| 3 | 8,902 |
| 4 | 197,281 |
| 5 | 4,865,609 |
| 6 | 119,060,324 |
| 7 | 3,195,901,860|
| 8 | 84,998,978,956|
| 9 | 2,439,530,234,167|
| 10 | 69,352,859,712,417|

The program has included 6 different starting positions for a more robust testing of move generations across different positions.

**Tips**: For most chess engines, perft is usually tested up to depth 6 or 7, as it takes exponentially longer for more than that.
