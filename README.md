# Ro.play()
My project for PAOJ

Structura/ ierarhia claselor:

Package Audit:
  public class Audit:
    - FILENAME\n
    -> public static void logInAudit(String actionName); //scrie in fisierul de audit

----------------------------------------------------------------------------------

Package Model:
  Package Enums:
    -public enum MusicGenre //genuri muzicale posibile
    
  public abstract class Account:
    -id
    -username
    -email
    password

  ~public class Administrator extends Account:
    -parolaAdmin

  ~public class User extends Account:
    -premium
    -descriere

  public abstract class Artist:
    -id
    -name
    -genre (MusicGenre)
    -contactEmail
    -description

  ~public classSoloArtist extends Artist:
    -country

  ~public class Band extends Artist:
    -List<SoloArtist> members

  public class Song implements Comparable<Song>:
    -id
    -title
    -artist (Artist) //SoloArtist sau Band
    -album
    -path

  public class Playlist:
    -id
    -name
    -LinkedHashSet<Song> songs
    -owner (User)

---------------------------------------------------------------------------------

Package Service:
  public class AccountService:
    -userService
    -AdministratorService
    -UserDAO
    -AdministratorDAO

    -> login() //logheaza ca user sau ca Administrator si returneaza obiectul utilizatorului curent

  public class AdministratorService:
    -AdministratorDAO
    -SongDAO
    -SoloArtistDAO
    -BandDAO

    -> login
    -> adminMenu

  public class UserService:
    -userDAO

    -> login
    -> signUp
    -> showLikedSongs
    -> recommendSong
    -> editAccount
    -> userMenu
    -> searchSongFlow
    -> listUserPlaylists
    -> playlistMenu
    -> createNewPlaylist

  public class SongService: //metodele folosite pentru implementarea redarii melodiei - preluate de pe: https://www.geeksforgeeks.org/play-audio-file-using-java/
    -clip
    -currentframe
    -status
    -audioInputStream
    -filePath
    -songDAO

    -> playSong
    -> shuffleAndPlayAll

------------------------------------------------------------------------------

package BazaDeDate.DAOs:
  public Interface DAOInterface:
    -> create
    -> read(id)
    -> readAll
    -> delete(id)
    -> update(T t)

  ~AdministratorDAO implements DAOInterface: //clasa Singleton
    -> findByUsername(username)

  ~UserDAO implements DAOInterface
    -> findByEmail
    -> findByUsername

  ~SoloArtistDAO implements DAOInterface
    
  ~BandDAO implements DAOInterface
    ->addMember
    ->removeMember

  ~SonDAO implements DAOInterface

  ~PlaylistDAO implements DAOInterface
    ->addSongToPlaylist
    ->removeSongFromPlaylist

  ~LikedSongsDAO //gestioneaza tabelul likedSongs_[username]
    ->addLike
    ->removeLike
    ->readAllLikded
    ->isLiked

  public class DatabaseConnection //stabileste conexiunea cu baza de date

-------------------------------------------------------------------------------

![image](https://github.com/user-attachments/assets/0e1ff054-76cc-49da-a21e-431b120a4747)

