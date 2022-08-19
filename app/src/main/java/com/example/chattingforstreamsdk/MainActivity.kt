package com.example.chattingforstreamsdk

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.chattingforstreamsdk.databinding.ActivityMainBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var client: ChatClient

    private val user = User(
        id = "funny",
        name = "gg",
        image = "https://ifh.cc/g/cKhWxt.jpg"
    )

    private var cnt = Random(1000)

    init {
        TransformStyle.messageListItemStyleTransformer =
            StyleTransformer { messageListItemStyle ->
                messageListItemStyle.copy(
                    textStyleDateSeparator = messageListItemStyle.textStyleDateSeparator,
                )
            }

        TransformStyle.messageInputStyleTransformer =
            StyleTransformer { messageInputStyle ->
                messageInputStyle.copy(
                    commandInputBadgeIcon = requireNotNull(getDrawable(R.drawable.ic_baseline_camera_alt_24)),

                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(),
            appContext = applicationContext
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

        val headerViewModel: ChannelListHeaderViewModel by viewModels()
        headerViewModel.bindView(binding.channelListHeaderView, this)

        binding.channelListView.setChannelItemClickListener { channel ->
            startActivity(MessageListActivity.newIntent(this, channel))
        }

//        binding.channelListView.setViewHolderFactory(CustomChannelListItemViewHolderFactory())
        binding.channelListHeaderView.apply {
            title = "채팅"
        }



    }

    private fun setNewChat() {
        binding.btnNewChat.setOnClickListener {
            client.createChannel(
                channelType = "messaging",
                channelId = "new_channel_${cnt.nextInt()}",
                memberIds = listOf(user.id, "ese111", "marvel"),
                extraData = mapOf("name" to "${cnt.nextInt()}", "image" to "https://ifh.cc/g/sSr5Rr.png")
            ).enqueue()
        }
    }
}