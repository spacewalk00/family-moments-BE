package com.spring.familymoments.domain.post;

import com.spring.familymoments.config.BaseException;
import com.spring.familymoments.config.BaseResponse;
import com.spring.familymoments.domain.post.model.MultiPostRes;
import com.spring.familymoments.domain.post.model.PostInfoReq;
import com.spring.familymoments.domain.post.model.PostReq;
import com.spring.familymoments.domain.post.model.SinglePostRes;
import com.spring.familymoments.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.spring.familymoments.config.BaseResponseStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    /**
     * 게시글 작성 API
     * [POST] /posts?familyId={가족인덱스}
     * @return BaseResponse<SinglePostRes>
     */
    @ResponseBody
    @PostMapping
    public BaseResponse<SinglePostRes> createPost(@AuthenticationPrincipal User user, @RequestParam("familyId") long familyId,
                                                  @RequestPart("postInfo") PostInfoReq postInfoReq,
                                                  @RequestPart("img1")MultipartFile img1,
                                                  @RequestPart(name = "img2", required = false)MultipartFile img2,
                                                  @RequestPart(name = "img3", required = false)MultipartFile img3,
                                                  @RequestPart(name = "img4", required = false)MultipartFile img4){
        if(postInfoReq == null) {
            return new BaseResponse<>(minnie_POSTS_EMPTY_POST_INFO);
        }

        if(postInfoReq.getContent() == null) {
            return new BaseResponse<>(minnie_POSTS_EMPTY_CONTENT);
        }

        List<MultipartFile> imgs = new ArrayList<>();

        Stream.of(img1, img2, img3, img4)
                .filter(img -> img != null)
                .forEach(img -> imgs.add(img));

        if(imgs.isEmpty()) {
            return new BaseResponse<>(minnie_POSTS_EMPTY_IMAGE);
        }

        System.out.println(user.getUserId());

        PostReq postReq = PostReq.builder().userId(user.getUserId())
                .familyId(familyId)
                .imgs(imgs)
                .content(postInfoReq.getContent())
                .build();

        try {
            SinglePostRes singlePostRes = postService.createPosts(user, postReq);
            return new BaseResponse<>(singlePostRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 최근 10개 게시글 조회 API
     * [GET] /posts?familyId={가족인덱스}
     * @return BaseResponse<List<MultiPostRes>>
     */
    @ResponseBody
    @GetMapping(params = {"familyId"})
    public BaseResponse<List<MultiPostRes>> getRecentPosts(@AuthenticationPrincipal User user, @RequestParam("familyId") long familyId) {
        try {
            List<MultiPostRes> multiPostRes = postService.getPosts(user.getUserId(), familyId);
            return new BaseResponse<>(multiPostRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 10개 게시글 조회 API
     * [GET] /posts?familyId={가족인덱스}&postId={이전 게시물의 최소 postId}
     * @return BaseResponse<List<MultiPostRes>>
     */
    @ResponseBody
    @GetMapping(params = {"familyId", "postId"})
    public BaseResponse<List<MultiPostRes>> getNextPosts(@AuthenticationPrincipal User user, @RequestParam("familyId") long familyId, @RequestParam("postId") long postId) {
        try {
            List<MultiPostRes> multiPostRes = postService.getPosts(user.getUserId(), familyId, postId);
            return new BaseResponse<>(multiPostRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
