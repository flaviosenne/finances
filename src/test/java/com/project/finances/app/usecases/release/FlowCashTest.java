package com.project.finances.app.usecases.release;

import com.project.finances.domain.entity.*;
import com.project.finances.domain.exception.BadRequestException;
import com.project.finances.app.usecases.category.repository.CategoryQuery;
import com.project.finances.app.usecases.release.dto.ReleaseDto;
import com.project.finances.app.usecases.release.repository.ReleaseCommand;
import com.project.finances.app.usecases.release.repository.ReleaseQuery;
import com.project.finances.app.usecases.user.repository.UserQuery;
import com.project.finances.mocks.dto.ReleaseDtoMock;
import com.project.finances.mocks.entity.ReleaseMock;
import com.project.finances.mocks.entity.UserMock;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FlowCashTest {
    @Mock
    private ReleaseCommand command;
    @Mock
    private UserQuery userQuery;
    @Mock
    private CategoryQuery categoryQuery;
    @Mock
    private ReleaseQuery query;

    private FlowCashProtocol flowCashProtocol;

    @BeforeEach
    void setup(){
        flowCashProtocol = new FlowCash(command,userQuery, categoryQuery, query);
    }

    // todo save release
    @Test
    @DisplayName("Should save a release when data is provider")
    void saveRelease(){
        Release releaseMock = ReleaseMock.get();
        User userMock = releaseMock.getUser();
        String userId = userMock.getId();
        ReleaseDto dto = ReleaseDtoMock.get();
        String categoryId = dto.getCategory().getId();

        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.of(userMock));
        when(categoryQuery.getCategoryByIdAndByUserId(categoryId, userMock.getId())).thenReturn(Optional.of(releaseMock.getCategory().withId(categoryId)));
        when(command.create(any(Release.class))).thenReturn(releaseMock);

        ReleaseDto result = flowCashProtocol.createRelease(dto, userId);

        BDDAssertions.assertThat(result).isNotNull().isInstanceOf(ReleaseDto.class);

        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, times(1)).getCategoryByIdAndByUserId(categoryId, releaseMock.getUser().getId());
        verify(command, times(1)).create(any(Release.class));
    }

    @Test
    @DisplayName("Should throw bad request exception when try save release and user is not provider or invalid")
    void saveNotFoundUser(){
        String userId = "user-id-invalid";

        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.empty());

        ReleaseDto dto = ReleaseDtoMock.get();

        Throwable exception = BDDAssertions.catchThrowable(()->flowCashProtocol.createRelease(dto, userId));

        BDDAssertions.assertThat(exception).isInstanceOf(BadRequestException.class).hasMessage("Usuário não encontrado");

        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, never()).getCategoryById(anyString());
        verify(command, never()).create(any(Release.class));
    }

    @Test
    @DisplayName("Should throw bad request exception when try save release and  category is not provider or invalid")
    void saveNotFoundCategory(){
        String userId = "user-id-valid";
        User user = UserMock.get();
        ReleaseDto dto = ReleaseDtoMock.get();
        String categoryId = dto.getCategory().getId();

        when(categoryQuery.getCategoryById(categoryId)).thenReturn(Optional.empty());
        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.of(user));


        Throwable exception = BDDAssertions.catchThrowable(()->flowCashProtocol.createRelease(dto, userId));

        BDDAssertions.assertThat(exception).isInstanceOf(BadRequestException.class).hasMessage("Categoria não exitente para esse usuario");

        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, times(1)).getCategoryByIdAndByUserId(categoryId, user.getId());
        verify(command, never()).create(any(Release.class));
    }

    //todo list releases
    @Test
    @DisplayName("Should return a list pageable of releases when request is successful")
    void list(){
        Pageable pageMock = Mockito.mock(Pageable.class);
        String userId = "id-valid";

        Release releaseMock = ReleaseMock.get();

        when(query.getReleases(userId, pageMock)).thenReturn(new PageImpl<Release>(Arrays.asList(releaseMock)));

        Page<Release> result = flowCashProtocol.listReleases(userId, pageMock);


        BDDAssertions.assertThat(result).isNotNull();
        BDDAssertions.assertThat(result.getContent()).isNotNull().hasSize(1);
        BDDAssertions.assertThat(result.getContent().get(0)).isEqualTo(releaseMock);

        verify(query, times(1)).getReleases(userId, pageMock);

    }

    // todo update release
    @Test
    @DisplayName("Should update release in DB when exist id and category is valid and is owner user")
    void update(){

        User userMock = UserMock.get();
        ReleaseDto dto = ReleaseDtoMock.get();
        Release releaseMock = ReleaseMock.get();
        String userId = "valid-id-user";

        when(query.findReleaseById(dto.getId(), userId)).thenReturn(Optional.of(releaseMock.withId(dto.getId())));
        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.of(userMock.withId(userId)));
        when(categoryQuery.getCategoryByIdAndByUserId(dto.getCategory().getId(), userId))
                .thenReturn(Optional.of(releaseMock.getCategory().withId(dto.getCategory().getId())));

        when(command.update(any(Release.class), eq(dto.getId()))).thenReturn(Release.builder()
                        .category(releaseMock.getCategory().withId(dto.getCategory().getId()))
                        .typeRelease(TypeRelease.EXPENSE.name()).statusRelease(StatusRelease.PENDING.name()).build().withId(dto.getId()));

        ReleaseDto result = flowCashProtocol.updateRelease(dto,userId);

        BDDAssertions.assertThat(result).isNotNull();
        BDDAssertions.assertThat(result.getId()).isNotNull().isEqualTo(dto.getId());
        BDDAssertions.assertThat(result.getCategory().getId()).isNotNull().isEqualTo(dto.getCategory().getId());

        verify(query, times(1)).findReleaseById(dto.getId(), userId);
        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, times(1)).getCategoryByIdAndByUserId(dto.getCategory().getId(),userId);
        verify(command, times(1)).update(any(Release.class),eq(dto.getId()));


    }


    @Test
    @DisplayName("Should throw bad request exception when try update release and user is not provider or invalid")
    void updateNotFoundRelease(){
        String id = "invalid-id-release";
        ReleaseDto dto = ReleaseDto.builder().id(id).build();
        String userId = "user-id-valid";

        when(query.findReleaseById(id, userId)).thenReturn(Optional.empty());

        Throwable exception = BDDAssertions.catchThrowable(() -> flowCashProtocol.updateRelease(dto, userId));


        BDDAssertions.assertThat(exception).isInstanceOf(BadRequestException.class).hasMessage("Lançamento não encontrado");

        verify(query, times(1)).findReleaseById(id, userId);
        verify(userQuery, never()).findByIdIsActive(anyString());
        verify(categoryQuery, never()).getCategoryByIdAndByUserId(anyString(), anyString());
        verify(command, never()).create(any(Release.class));
    }

    @Test
    @DisplayName("Should throw bad request exception when try update release and user is not provider or invalid")
    void updateNotFoundUser(){
        ReleaseDto dto = ReleaseDtoMock.get();
        String userId = "user-id-invalid";

        when(query.findReleaseById(dto.getId(), userId)).thenReturn(Optional.of(Release.builder().build().withId(dto.getId())));
        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.empty());

        Throwable exception = BDDAssertions.catchThrowable(() -> flowCashProtocol.updateRelease(dto, userId));

        BDDAssertions.assertThat(exception).isInstanceOf(BadRequestException.class).hasMessage("Usuário não encontrado");

        verify(query, times(1)).findReleaseById(dto.getId(), userId);
        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, never()).getCategoryByIdAndByUserId(anyString(), anyString());
        verify(command, never()).create(any(Release.class));
    }

    @Test
    @DisplayName("Should throw bad request exception when try update release and  category is not provider or invalid")
    void updateNotFoundCategory(){
        ReleaseDto dto = ReleaseDtoMock.get();
        String userId = "user-id-valid";

        when(query.findReleaseById(dto.getId(), userId)).thenReturn(Optional.of(Release.builder().build().withId(dto.getId())));
        when(userQuery.findByIdIsActive(userId)).thenReturn(Optional.of(UserMock.get().withId(userId)));
        when(categoryQuery.getCategoryByIdAndByUserId(dto.getCategory().getId(), userId)).thenReturn(Optional.empty());

        Throwable exception = BDDAssertions.catchThrowable(() -> flowCashProtocol.updateRelease(dto, userId));

        BDDAssertions.assertThat(exception).isInstanceOf(BadRequestException.class).hasMessage("Categoria não exitente para esse usuario");

        verify(query, times(1)).findReleaseById(dto.getId(), userId);
        verify(userQuery, times(1)).findByIdIsActive(userId);
        verify(categoryQuery, times(1)).getCategoryByIdAndByUserId(dto.getCategory().getId(), userId);
        verify(command, never()).create(any(Release.class));
    }



}