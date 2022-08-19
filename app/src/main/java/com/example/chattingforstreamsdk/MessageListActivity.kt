package com.example.chattingforstreamsdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.chattingforstreamsdk.databinding.ActivityMessageListBinding
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.message.composer.viewmodel.MessageComposerViewModel
import io.getstream.chat.android.ui.message.composer.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import kotlinx.coroutines.launch

class MessageListActivity : AppCompatActivity() {

    private val binding: ActivityMessageListBinding by lazy {
        ActivityMessageListBinding.inflate(layoutInflater)
    }

    @OptIn(ExperimentalStreamChatApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val cid = checkNotNull(intent.getStringExtra(CID_KEY)) {
            "MessageListActivity를 시작하기 위해서는 채널 아이디 (cid) 정보가 필요합니다."
        }

        // Step 1 - 채팅방 컴포넌트에 연결할 뷰모델 초기화
        val factory = MessageListViewModelFactory(cid)
//        val messageListHeaderViewModel: MessageListHeaderViewModel by viewModels { factory }
        val messageListViewModel: MessageListViewModel by viewModels { factory }
        val messageInputViewModel: MessageInputViewModel by viewModels { factory }
        val messageComposerViewModel: MessageComposerViewModel by viewModels { factory }

        // Step 2 - 채팅방 컴포넌트에 뷰모델 연결
//        messageListHeaderViewModel.bindView(binding.messageListHeaderView, this)
        messageListViewModel.bindView(binding.messageListView, this)
//        messageInputViewModel.bindView(binding.messageInputView, this)
        messageComposerViewModel.bindView(binding.messageComposerView, this)

        // Step 3 - MessageListHeaderView와 MessageInputView 연결 및 채팅 쓰레드 모드 업데이트
        messageListViewModel.mode.observe(this) { mode ->
            when (mode) {
                is MessageListViewModel.Mode.Thread -> {
//                    messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                    messageComposerViewModel.setMessageMode(MessageMode.MessageThread(mode.parentMessage))
                    messageInputViewModel.setActiveThread(mode.parentMessage)

                }
                is MessageListViewModel.Mode.Normal -> {
                    Log.d("FunnyApp" , "${mode.toString()}")
//                    messageListHeaderViewModel.resetThread()
                    messageComposerViewModel.leaveThread()
                    messageInputViewModel.resetThread()
                }
            }
        }

        // Step 4 - 채팅방 동작에 대한 이벤트 구독 및 NavigateUp 이벤트 발생 시 채팅방 종료
        messageListViewModel.state.observe(this) { state ->
            if (state is MessageListViewModel.State.NavigateUp) {
                finish()
            }
        }

        // Step 5 - 채팅방 헤더의 뒤로가기 버튼 및 디바이스 백버튼 터치 시 NavigateUp 이벤트 송출
        val backHandler = {
            messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
        }
        binding.messageListHeaderView.setNavigationOnClickListener {
            backHandler()
        }
        onBackPressedDispatcher.addCallback(this) {
            backHandler()
        }

        binding.messageListView.setMessageReplyHandler { _, message ->
            messageComposerViewModel.performMessageAction(Reply(message))
        }
        binding.messageListView.setMessageEditHandler { message ->
            messageComposerViewModel.performMessageAction(Edit(message))
        }

        messageListViewModel.targetMessage.observe(this) {
            Log.d("FunnyApp" , "${it.text}")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                messageListViewModel.channelState.collect {
                    it?.messages?.value?.forEach {
                        Log.d("FunnyApp" , "${it.text}")
                    }
                    Log.d("FunnyApp" , "${it?.messages?.value?.size}")
                    it?.messages?.collect { list ->
                        if(list.isNotEmpty()) {
                            binding.instanceMessage.visibility = View.GONE
                        }
                    }
                }
            }
        }
        binding.messageComposerView.setLeadingContent(CustomMessageComposerLeadingContent(this))
        binding.messageComposerView.setLeadingContent(
            CustomMessageComposerLeadingContent(this).also {
                it.datePickerButtonClickListener = {
                    binding.messageComposerView.attachmentsButtonClickListener()
                }
            }
        )

        binding.one.setOnClickListener { message ->
            message as TextView
            messageComposerViewModel.sendMessage(messageComposerViewModel.buildNewMessage(message.text.toString()))
        }

        binding.two.setOnClickListener { message ->
            message as TextView
            messageComposerViewModel.sendMessage(messageComposerViewModel.buildNewMessage(message.text.toString()))
        }
    }

    companion object {
        // MessageListActivity의 인텐트 생성 및 채팅방의 cid 정보 전달
        private const val CID_KEY = "key:cid"
        fun newIntent(context: Context, channel: Channel): Intent =
            Intent(context, MessageListActivity::class.java).putExtra(CID_KEY, channel.cid)
    }
}