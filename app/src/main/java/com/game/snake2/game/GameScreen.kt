package com.game.snake2.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Difficulty {
    EASY, MEDIUM, HARD
}

@Composable
fun GameScreen() {
    var gameState by remember { mutableStateOf(GameState()) }
    var isGameOver by remember { mutableStateOf(false) }
    var isWinner by remember { mutableStateOf(false) }
    var isTimeUp by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf(Difficulty.EASY) }
    var gameSpeed by remember { mutableStateOf(600L) }
    var timeLeft by remember { mutableStateOf(30) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
    // Single game loop
    LaunchedEffect(key1 = Unit) {
        while (true) {
            if (!isGameOver && !isWinner && !isTimeUp) {
                delay(gameSpeed)
                gameState = gameState.update()
                if (gameState.checkCollision()) {
                    isGameOver = true
                }
                if (gameState.isWinner()) {
                    isWinner = true
                }
            } else {
                delay(100) // Small delay to prevent busy waiting
            }
        }
    }
    
    // Single timer loop
    LaunchedEffect(key1 = Unit) {
        while (true) {
            if (!isGameOver && !isWinner && !isTimeUp) {
                delay(1000)
                timeLeft--
                if (timeLeft <= 0) {
                    isTimeUp = true
                }
            } else {
                delay(100) // Small delay to prevent busy waiting
            }
        }
    }
    
    LaunchedEffect(difficulty) {
        gameSpeed = when (difficulty) {
            Difficulty.EASY -> 600L
            Difficulty.MEDIUM -> 400L
            Difficulty.HARD -> 300L
        }
        timeLeft = when (difficulty) {
            Difficulty.EASY -> 50
            Difficulty.MEDIUM -> 30
            Difficulty.HARD -> 20
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 1.dp, end = 1.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Food: ${gameState.score}/${gameState.getRequiredFood()}",
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Time: ${timeLeft}s",
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                color = if (timeLeft <= 5) Color.Red else MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Level: ${difficulty.name}",
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.LightGray)
                .focusable()
        ) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
            ) {
                // Draw snake
                gameState.snake.forEachIndexed { index, position ->
                    val isHead = index == 0
                    if (isHead) {
                        // Draw snake head
                        // Main head shape
                        drawRect(
                            color = Color(0xFFFFB74D), // Orange color for dragon head
                            topLeft = Offset(position.x * 40f, position.y * 40f),
                            size = androidx.compose.ui.geometry.Size(40f, 40f)
                        )
                        // Eyes
                        drawCircle(
                            color = Color(0xFF000000), // Black eyes
                            radius = 4f,
                            center = Offset(position.x * 40f + 12f, position.y * 40f + 15f)
                        )
                        drawCircle(
                            color = Color(0xFF000000),
                            radius = 4f,
                            center = Offset(position.x * 40f + 28f, position.y * 40f + 15f)
                        )
                    } else {
                        // Draw snake body segments with alternating colors
                        val segmentColor = if (index % 2 == 0) {
                            Color(0xFF03A9F4) // Light blue
                        } else {
                            Color(0xFF0288D1) // Darker blue
                        }
                        // Draw rounded rectangle for body segment
                        drawRoundRect(
                            color = segmentColor,
                            topLeft = Offset(position.x * 40f + 2f, position.y * 40f + 2f),
                            size = androidx.compose.ui.geometry.Size(36f, 36f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                    }
                }
                
                // Draw food
                gameState.food.forEach { position ->
                    // Draw apple body (red circle)
                    drawCircle(
                        color = Color(0xFFE53935), // Apple red
                        radius = 16f,
                        center = Offset(position.x * 40f + 20f, position.y * 40f + 20f)
                    )
                    
                    // Draw apple stem (brown rectangle)
                    drawRect(
                        color = Color(0xFF795548), // Brown
                        topLeft = Offset(position.x * 40f + 19f, position.y * 40f + 4f),
                        size = androidx.compose.ui.geometry.Size(2f, 6f)
                    )
                    
                    // Draw apple leaf (green triangle)
                    val leafPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(position.x * 40f + 22f, position.y * 40f + 6f)
                        lineTo(position.x * 40f + 26f, position.y * 40f + 4f)
                        lineTo(position.x * 40f + 22f, position.y * 40f + 8f)
                        close()
                    }
                    drawPath(
                        path = leafPath,
                        color = Color(0xFF4CAF50) // Green
                    )
                }
                
                // Draw obstacles
                gameState.obstacles.forEach { position ->
                    drawRect(
                        color = Color(0xFF795548), // Brown color for obstacles
                        topLeft = Offset(position.x * 40f, position.y * 40f),
                        size = androidx.compose.ui.geometry.Size(40f, 40f)
                    )
                }

                // Draw countdown text
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${timeLeft}s",
                        size.width / 2,
                        size.height - 100f,
                        android.graphics.Paint().apply {
                            color = if (timeLeft <= 5) android.graphics.Color.RED else android.graphics.Color.BLACK
                            textSize = 80f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                // Draw food count text
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        "${gameState.score}/${gameState.getRequiredFood()}",
                        size.width / 2,
                        size.height - 50f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 80f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }

            TouchHandler(
                modifier = Modifier.fillMaxSize(),
                onDirectionChange = { newDirection ->
                    if (!isGameOver) {
                        gameState = gameState.copy(direction = newDirection)
                    }
                }
            )
        }
        
        if (isTimeUp) {
            Text(
                text = "Time's Up!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    gameState = GameState(difficulty = difficulty)
                    isGameOver = false
                    isWinner = false
                    isTimeUp = false
                    timeLeft = when (difficulty) {
                        Difficulty.EASY -> 50
                        Difficulty.MEDIUM -> 30
                        Difficulty.HARD -> 20
                    }
                    focusRequester.requestFocus()
                },
                modifier = Modifier.padding(5.dp)
            ) {
                Text("Play Again")
            }
        }
        
        if (isGameOver) {
            Text(
                text = "Game Over!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    gameState = GameState(difficulty = difficulty)
                    isGameOver = false
                    isWinner = false
                    isTimeUp = false
                    timeLeft = when (difficulty) {
                        Difficulty.EASY -> 50
                        Difficulty.MEDIUM -> 30
                        Difficulty.HARD -> 20
                    }
                    focusRequester.requestFocus()
                },
                modifier = Modifier.padding(5.dp)
            ) {
                Text("Play Again")
            }
        }
        
        if (isWinner) {
            Text(
                text = "Winner!",
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = {
                    gameState = GameState(difficulty = difficulty)
                    isGameOver = false
                    isWinner = false
                    isTimeUp = false
                    timeLeft = when (difficulty) {
                        Difficulty.EASY -> 50
                        Difficulty.MEDIUM -> 30
                        Difficulty.HARD -> 20
                    }
                    focusRequester.requestFocus()
                },
                modifier = Modifier.padding(5.dp)
            ) {
                Text("Play Again")
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Difficulty.values().forEach { diff ->
                Button(
                    onClick = { 
                        difficulty = diff
                        gameState = GameState(difficulty = diff)
                        isGameOver = false
                        isWinner = false
                        isTimeUp = false
                    },
                    modifier = Modifier.padding(5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (difficulty == diff) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(diff.name)
                }
            }
        }
    }
}

data class Position(val x: Int, val y: Int)
data class GameState(
    val snake: List<Position> = listOf(Position(5, 5)),
    val food: List<Position> = listOf(Position(10, 10)),
    val direction: Direction = Direction.RIGHT,
    val obstacles: List<Position> = generateObstacles(Difficulty.EASY),
    val score: Int = 0,
    val difficulty: Difficulty = Difficulty.EASY
) {
    fun getRequiredFood(): Int = when (difficulty) {
        Difficulty.EASY -> 5
        Difficulty.MEDIUM -> 10
        Difficulty.HARD -> 15
    }

    fun isWinner(): Boolean = score >= getRequiredFood()
    
    fun update(): GameState {
        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Position(head.x, (head.y - 1 + 20) % 20)
            Direction.DOWN -> Position(head.x, (head.y + 1) % 20)
            Direction.LEFT -> Position((head.x - 1 + 20) % 20, head.y)
            Direction.RIGHT -> Position((head.x + 1) % 20, head.y)
        }
        
        // Check if snake is stuck (same position for 3 moves)
        val isStuck = snake.size >= 3 && snake.take(3).all { it == newHead }
        
        // Add more food if snake is stuck
        val newFood = if (isStuck) {
            generateFood(snake + obstacles + food)
        } else {
            food
        }
        
        val newSnake = if (newHead in food) {
            listOf(newHead) + snake
        } else {
            listOf(newHead) + snake.dropLast(1)
        }
        
        return copy(
            snake = newSnake,
            food = if (newHead in food) {
                generateFood(newSnake + obstacles + newFood.filter { it != newHead })
            } else {
                newFood
            },
            score = if (newHead in food) score + 1 else score
        )
    }
    
    fun checkCollision(): Boolean {
        val head = snake.first()
        return snake.drop(1).contains(head) ||
               obstacles.contains(head)
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

private fun generateObstacles(difficulty: Difficulty): List<Position> {
    val obstacleCount = when (difficulty) {
        Difficulty.EASY -> 3
        Difficulty.MEDIUM -> 5
        Difficulty.HARD -> 8
    }
    return List(obstacleCount) {
        Position(Random.nextInt(0, 20), Random.nextInt(0, 20))
    }
}

private fun generateFood(existingPositions: List<Position>): List<Position> {
    val foodCount = when (existingPositions.size) {
        in 0..5 -> 1
        in 6..10 -> 1
        else -> 1
    }
    
    val newFood = mutableListOf<Position>()
    repeat(foodCount) {
    var position: Position
    do {
        position = Position(Random.nextInt(0, 20), Random.nextInt(0, 20))
        } while (existingPositions.contains(position) || newFood.contains(position))
        newFood.add(position)
    }
    return newFood
} 