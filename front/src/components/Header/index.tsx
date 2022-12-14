import { useContext, useRef, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AxiosError } from 'axios';

import Dropdown from '@components/Dropdown';
import Conditional from '@components/Conditional';
import Modal from '@components/Modal';
import useOutsideClick from '@hooks/useOutsideClick';
import useBoolean from '@hooks/useBoolean';
import { UserStateContext, UserDispatchContext } from '@context/UserProvider';
import { ROUTES, MAX_LENGTH } from '@constants/index';
import * as S from './styles';

import LogoIcon from '@assets/logo.svg';
import { api } from '@api/index';

const Header = () => {
  const navigate = useNavigate();
  const profileRef = useRef(null);
  const { userData } = useContext(UserStateContext);
  const dispatch = useContext(UserDispatchContext);
  const [isActive, setIsActive] = useOutsideClick(profileRef, false);
  const { value: isOpenModal, setTrue: openModal, setFalse: closeModal } = useBoolean();
  const [nickName, setNickName] = useState('');

  const handleLogout = () => {
    dispatch({ type: 'DELETE_USER' });
    navigate(ROUTES.HOME);
  };

  const toggleDropdown = () => {
    setIsActive(!isActive);
  };

  const handleModifyNickname = async () => {
    return;
  };

  const handleChangeInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    setNickName(e.target.value);
  };

  const handleOpenModal = () => {
    openModal();
    setNickName('');
  };

  const handleChangeRole = async () => {
    if (!userData) return;

    try {
      const role = userData.role === 'COACH' ? 'CREW' : 'COACH';
      const { data } = await api.post('/api/auth/login/v2', {
        name: userData.name,
        role,
      });
      dispatch({ type: 'SET_USER', userData: data });
      navigate(`/${role.toLowerCase()}`, { replace: true });
    } catch (error) {
      if (error instanceof AxiosError) {
        alert(error.response?.data?.message);
        console.log(error);
      }
    }
  };

  return (
    <S.HeaderContainer>
      <S.LogoLink to={userData ? `/${userData.role.toLowerCase()}` : ROUTES.HOME}>
        <S.LogoImage src={LogoIcon} alt="????????? ??????" />
        <h1>?????????</h1>
      </S.LogoLink>
      {!userData && (
        <S.MainButton onClick={() => (location.href = 'https://teatime.pe.kr/')}>
          ?????? ??????
        </S.MainButton>
      )}
      {userData && (
        <S.ProfileContainer>
          <S.RoleButton onClick={handleChangeRole} isRole={userData.role === 'COACH'}>
            {userData.role === 'COACH' ? '??????' : '??????'}
            <div />
          </S.RoleButton>
          <S.ProfileWrapper ref={profileRef} onClick={toggleDropdown}>
            <span>{userData.name}</span>
            <img src={userData.image} alt="????????? ?????????" />
          </S.ProfileWrapper>
          <Dropdown isActive={isActive}>
            <Conditional condition={userData.role === 'COACH'}>
              <Link to={ROUTES.COACH_HISTORY}>
                <li>????????????</li>
              </Link>
              <Link to={ROUTES.SCHEDULE}>
                <li>????????? ??????</li>
              </Link>
              <Link to={ROUTES.QUESTION}>
                <li>???????????? ??????</li>
              </Link>
              <Link to={ROUTES.COACH_PROFILE}>
                <li>????????? ??????</li>
              </Link>
            </Conditional>

            <Conditional condition={userData.role === 'CREW'}>
              <Link to={ROUTES.CREW_HISTORY}>
                <li>????????????</li>
              </Link>
              <li onClick={handleOpenModal}>????????? ??????</li>
            </Conditional>
            <li onClick={handleLogout}>????????????</li>
          </Dropdown>
        </S.ProfileContainer>
      )}
      {isOpenModal && (
        <Modal
          title="????????? ??????"
          firstButtonName="????????????"
          secondButtonName="????????????"
          onClickFirstButton={closeModal}
          onClickSecondButton={handleModifyNickname}
          closeModal={closeModal}
        >
          <S.Input
            autoFocus
            maxLength={MAX_LENGTH.NAME}
            type="text"
            placeholder="?????? ?????????????????? ????????? ??? ?????????."
            value={nickName}
            onChange={(e) => handleChangeInput(e)}
            disabled
            required
          />
        </Modal>
      )}
    </S.HeaderContainer>
  );
};

export default Header;
