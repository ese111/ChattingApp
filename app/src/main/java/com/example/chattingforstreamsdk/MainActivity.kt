package com.example.chattingforstreamsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.chattingforstreamsdk.databinding.ActivityMainBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var client: ChatClient

    private val user = User(
        id = "marvel",
        name = "Iron Man",
        image = "https://bit.ly/2TIt8NR"
    )

    private var cnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(),
            appContext = applicationContext,
        )

        client = ChatClient.Builder(BuildConfig.SDK_KEY, applicationContext)
            .withPlugin(offlinePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()


        val token = client.devToken(user.id) // developer 토큰 생성
        client.connectUser( // 유저 로그인
            user = user,
            token = token
        ).enqueue {
            // Step 4 - 새로운 그룹 (채널) 생성
            if (it.isSuccess) {
                setNewChat()
            }
        }

        // Step 5 - ChannelListViewModel 생성 및 ChannelListView과 연동
        val viewModelFactory = ChannelListViewModelFactory()
        val viewModel: ChannelListViewModel by viewModels { viewModelFactory }
        viewModel.bindView(binding.channelListView, this)
        binding.channelListView.setChannelItemClickListener { channel ->
            startActivity(MessageListActivity.newIntent(this, channel))
        }

    }

    private fun setNewChat() {
        binding.btnNewChat.setOnClickListener {
            client.createChannel(
                channelType = "messaging",
                channelId = "new_channel_$cnt",
                memberIds = listOf(user.id),
                extraData = mapOf("name" to "$cnt New Channel")
            ).enqueue()
            cnt++
        }
    }
}