package com.rozie.rockpaperscissors

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.game_item.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GameActivity : AppCompatActivity() {
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var gameRepository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gameRepository = GameRepository(this)

        initViews()
    }

    private fun initViews() {
        setStatistics()
        setListeners()
    }

    private fun setStatistics() {
        mainScope.launch {
            val wins = withContext(Dispatchers.IO) {
                gameRepository.getCountOf("won")
            }
            val lost = withContext(Dispatchers.IO) {
                gameRepository.getCountOf("lost")
            }
            val draw = withContext(Dispatchers.IO) {
                gameRepository.getCountOf("draw")
            }
            tvStandings.text = getString(R.string.standings, wins, draw, lost)
            }

    }

    private fun setListeners() {
        ivRock.setOnClickListener { gameTurn(0) }
        ivPaper.setOnClickListener { gameTurn(1) }
        ivScissors.setOnClickListener { gameTurn(2) }
    }

    // 0 = Rock, 1 = Paper, 2 = Sissors
    private fun gameTurn(playerMove: Int) {
        when (playerMove) {
            0 -> ivPlayerMove.setImageDrawable(this.getDrawable(R.drawable.rock))
            1 -> ivPlayerMove.setImageDrawable(this.getDrawable(R.drawable.paper))
            2 -> ivPlayerMove.setImageDrawable(this.getDrawable(R.drawable.scissors))
            else ->
                return
        }
        val computerMove = computerMove()
        val result = checkGame(playerMove, computerMove)

        addDate(playerMove, computerMove, result)
        setStatistics()
    }

    private fun addDate(playerMove: Int, computerMove: Int, result: String){
        mainScope.launch {
            val game = Game(
                date = Date().toString(),
                playerMove = playerMove,
                computerMove = computerMove,
                gameResult = result
            )
            withContext(Dispatchers.IO) {
                gameRepository.insertGame(game)
            }
        }
    }

    private fun checkGame(playerMove: Int, computerMove: Int): String {
        //Sanity check:
        // draw = both 0, 1 or 2 (equals)
        // pWin = p=0 c=1, p=1 c=2, p=2 c=0
        // cWin = p=0 c=2, p=1 c=0, p=2 c=1
        val cResult = computerMove
        val pResult = playerMove
        return if (cResult == pResult) {
            tvResult.text = "It's a draw!"
            "draw"
        } else if (pResult == 0) {
            if (cResult == 1) {
                tvResult.text = "You have won!"
                "won"
            } else {        // (cResult == 2)
                tvResult.text = "Computer has won!"
                "lost"
            }
        } else if (pResult == 1) {
            if (cResult == 2) {
                tvResult.text = "You have won!"
                "won"
            } else {        // (cResult == 0)
                tvResult.text = "Computer has won!"
                "lost"
            }
        } else {           // (pResult == 2)
            if (cResult == 2) {
                tvResult.text = "You have won!"
                "won"
            } else {        // (cResult == 1)
                tvResult.text = "Computer has won!"
                "lost"
            }
        }
    }

    private fun computerMove(): Int {
        val randomInteger = (0..2).random()
        when (randomInteger) {
            0 -> ivComputerMove.setImageDrawable(this.getDrawable(R.drawable.rock))
            1 -> ivComputerMove.setImageDrawable(this.getDrawable(R.drawable.paper))
            2 -> ivComputerMove.setImageDrawable(this.getDrawable(R.drawable.scissors))
        }
        return randomInteger
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history_games -> {
                startGameHistoryActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startGameHistoryActivity() {
        val intent = Intent(this, GameHistoryActivity::class.java)
        startActivity(intent)
    }
}
